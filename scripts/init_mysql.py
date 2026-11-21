"""Import sql/init.sql into a new gdpm database; credentials never leave process memory."""
import os
from pathlib import Path
import shutil
import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8')
PROJECT_DIR = Path(__file__).resolve().parent.parent
username = os.environ.get('GDPM_DB_USERNAME')
password = os.environ.get('GDPM_DB_PASSWORD')
if not username or not password:
    raise SystemExit('需要环境变量 GDPM_DB_USERNAME 和 GDPM_DB_PASSWORD。')
client = os.environ.get('GDPM_MYSQL_CLIENT') or shutil.which('mysql')
if not client and os.name == 'nt':
    matches = sorted((Path(os.environ.get('ProgramFiles', 'C:/Program Files')) / 'MySQL').glob('*/bin/mysql.exe'))
    client = str(matches[-1]) if matches else None
if not client:
    raise SystemExit('请把 mysql 客户端加入 PATH，或设置 GDPM_MYSQL_CLIENT。')
env = os.environ.copy()
env['MYSQL_PWD'] = password
args = [client, '--protocol=TCP', '--host=127.0.0.1', '--port=3306', '--user=' + username,
        '--default-character-set=utf8mb4', '--batch', '--skip-column-names']

def execute(sql):
    result = subprocess.run(args, input=sql, capture_output=True, env=env)
    if result.returncode:
        message = result.stderr.decode('utf-8', errors='replace')
        for secret in (username, password):
            message = message.replace(secret, '[REDACTED]')
        raise SystemExit(message)
    return result.stdout.decode('utf-8').strip()

version = execute(b'SELECT VERSION();')
if not version.startswith('8.'):
    raise SystemExit('需要 MySQL 8，实际版本：' + version)
exists = execute(b"SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='gdpm';")
if exists != '0':
    raise SystemExit('gdpm 已存在。为保护数据已停止；请先备份并选择全新测试实例。')
execute((PROJECT_DIR / 'sql/init.sql').read_bytes())
print('MySQL ' + version + ' 初始化成功：' + execute(
    b"SELECT default_character_set_name FROM information_schema.schemata WHERE schema_name='gdpm';"))
print('业务表数量：' + execute(b"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='gdpm';"))
