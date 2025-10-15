http://dev.jdl.com:8123/api/swagger-ui/index.html

设置远程git地址
git remote set-url origin https://github.com/apollyon0728/my-ai-agent.git

使用postgresql，先创建vector扩展
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS public.vector_store (
id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
content TEXT,
metadata JSONB,
embedding VECTOR(1536)
);


PostgreSQL 相关SHELL命令
996  sudo -u postgres /Library/PostgreSQL/18/bin/pg_ctl -D /Library/PostgreSQL/18/data stop 2>/dev/null || true
997  sudo -u postgres /Library/PostgreSQL/18/bin/pg_ctl \\n  -D /Library/PostgreSQL/18/data \\n  start
998  sudo -u postgres /Library/PostgreSQL/18/bin/pg_ctl \\n  -D /Library/PostgreSQL/18/data \\n  status
999  sudo -u postgres /Library/PostgreSQL/18/bin/pg_ctl \\n  -D /Library/PostgreSQL/18/data \\n  stop
1000  sudo -u postgres /Library/PostgreSQL/18/bin/pg_ctl \\n  -D /Library/PostgreSQL/18/data \\n  start