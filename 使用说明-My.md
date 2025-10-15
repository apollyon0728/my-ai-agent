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