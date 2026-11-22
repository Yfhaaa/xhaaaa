"""只读检查后端规范化约束；报告写入 backend/target，不访问开发实例。"""
from pathlib import Path
import json
import re
import zipfile


BACKEND = Path(__file__).resolve().parents[1]
MAIN = BACKEND / "src/main/java/edu/gengdan/gdpm"
JAVA_FILES = sorted((BACKEND / "src").rglob("*.java"))


def mask_literals(source):
    """屏蔽注释和字符串，避免把 SQL、包声明示例等误认为 Java 类型引用。"""
    return re.sub(
        r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/',
        lambda match: " " * len(match[0]), source,
    )


def remove_annotations(source):
    """去掉可跨行、含嵌套括号的注解，以核对 Javadoc 与声明直接关联。"""
    source = mask_literals(source)
    while True:
        match = re.search(r"@[\w.]+", source)
        if match is None:
            return source
        end = match.end()
        while end < len(source) and source[end].isspace():
            end += 1
        if end < len(source) and source[end] == "(":
            depth = 1
            end += 1
            while end < len(source) and depth:
                depth += (source[end] == "(") - (source[end] == ")")
                end += 1
        source = source[:match.start()] + source[end:]


def verify():
    """逐项汇总通配符、类型、DTO、状态、注释和格式问题，不更改源码。"""
    failures = {name: [] for name in [
        "显式导入且无未使用导入", "无行内全限定类型", "写接口使用带Valid的DTO",
        "实体构造不使用反射工具", "服务和控制器返回类型明确", "业务状态使用枚举",
        "公共类型和方法有中文Javadoc", "源码单行不超过120字符",
    ]}
    labels = re.findall(r'\w+\("([^"]+)"\)', (MAIN / "common/BusinessStatus.java").read_text(encoding="utf-8"))
    for path in JAVA_FILES:
        source = path.read_text(encoding="utf-8-sig")
        label = str(path.relative_to(BACKEND))
        body = re.sub(r"(?m)^\s*(?:package|import) [^;]+;", "", source)
        code = mask_literals(body)

        def issue(name, detail):
            failures[name].append(label + ": " + detail)

        for match in re.finditer(r"(?m)^import (static )?([\w.*]+);", source):
            static, imported = match.groups()
            name = imported.rsplit(".", 1)[1]
            if "*" in imported or not re.search(r"\b" + re.escape(name) + r"\b", code):
                issue("显式导入且无未使用导入", imported)
            elif static and not re.search(r"(?<![\w.])" + name + r"\s*\(", code):
                issue("显式导入且无未使用导入", imported)
        if re.search(r"\b(?:[a-z]\w*\.)+[A-Z]\w*", code):
            issue("无行内全限定类型", "存在未导入的全限定类型")
        if path.parent.name == "controller":
            for match in re.finditer(r"@RequestBody\s+([^\n,)]+)", source):
                if not re.match(r"\w+Request\s+\w+", match[1]) or "@Valid" not in source[max(0, match.start()-20):match.start()]:
                    issue("写接口使用带Valid的DTO", match[1])
        if re.search(r"\bentity\(\s*\w+\.class|\bParams\.", code) or path.stem == "Params":
            issue("实体构造不使用反射工具", "仍有反射构造或Params解析")
        if any(part in path.parts for part in ["service", "controller"]):
            if re.search(r"\bObject\s+\w+\s*\(|Result<Object>", code):
                issue("服务和控制器返回类型明确", "存在Object返回值")
            if re.search(r"Map<String, Object>\s+(?:body|b)\b", code):
                issue("写接口使用带Valid的DTO", "存在Map请求体")
        if path.is_relative_to(MAIN) and path.stem != "BusinessStatus":
            # 保留提示语、消息标题；仅精确的状态值必须引用枚举。
            for status in labels:
                if '"' + status + '"' in source:
                    issue("业务状态使用枚举", status)
        declarations = list(re.finditer(
            r"(?m)^\s*public\s+(?:(?:abstract|final|static)\s+)*(?:class|interface|enum|@interface)\s+\w+"
            r"|^\s*public\s+[^;={\n]+\(", source,
        ))
        if path.parent.name == "service":
            declarations += list(re.finditer(r"(?m)^    (?!public\b)[\w<>?, .\[\]]+\s+\w+\([^;{]*\);", source))
        docs = list(re.finditer(r"/\*\*[\s\S]*?\*/", source))
        for declaration in declarations:
            previous = [doc for doc in docs if doc.end() <= declaration.start()]
            doc = previous[-1] if previous else None
            if doc is None or remove_annotations(source[doc.end():declaration.start()]).strip() or not re.search(
                r"[\u4e00-\u9fff]", doc[0]
            ):
                issue("公共类型和方法有中文Javadoc", declaration[0].strip())
        for number, line in enumerate(source.splitlines(), 1):
            if len(line) > 120:
                issue("源码单行不超过120字符", str(number) + "行: " + str(len(line)))
    artifact = BACKEND / "target/gdpm-system-v3.jar"
    if artifact.exists():
        with zipfile.ZipFile(artifact) as archive:
            if any(name.endswith("/common/Params.class") for name in archive.namelist()):
                failures["实体构造不使用反射工具"].append("构建产物仍含已删除的 Params.class，请清理旧编译输出")
    checks = [dict(name=name, passed=not issues, violations=issues) for name, issues in failures.items()]
    result = dict(javaFiles=len(JAVA_FILES), passed=sum(row["passed"] for row in checks), total=len(checks), checks=checks)
    output = BACKEND / "target/qc-round4/normalization.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    for row in checks:
        print(("PASS " if row["passed"] else "FAIL ") + row["name"])
        for violation in row["violations"]:
            print("  " + violation)
    print(f"{result['passed']}/{result['total']}；Java文件 {result['javaFiles']}")
    return 0 if result["passed"] == result["total"] else 1


if __name__ == "__main__":
    raise SystemExit(verify())
