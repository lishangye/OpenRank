import os
import glob
from dataclasses import dataclass
from typing import List

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity


@dataclass
class DocChunk:
    """文档分块结构，保留来源和文本内容。"""
    source: str
    text: str


class SimpleRAG:
    """最小化 RAG：读取 docs，分块，使用 TF-IDF 做检索。"""

    def __init__(self, docs_dir: str = "docs", chunk_size: int = 400):
        self.docs_dir = docs_dir
        self.chunk_size = chunk_size
        self.chunks: List[DocChunk] = []
        self.vectorizer = TfidfVectorizer(stop_words=None)
        self.tfidf = None

    def _chunk_text(self, text: str) -> List[str]:
        """按段落粗略分块，控制每块字符数，避免过长。"""
        text = text.replace("\r", "")
        paras = [p.strip() for p in text.split("\n\n") if p.strip()]
        chunks = []
        buf = ""
        for p in paras:
            if len(buf) + len(p) + 1 <= self.chunk_size:
                buf = (buf + "\n\n" + p).strip()
            else:
                if buf:
                    chunks.append(buf)
                buf = p
        if buf:
            chunks.append(buf)
        return chunks

    def build(self) -> None:
        """构建 TF-IDF 索引。"""
        paths = glob.glob(os.path.join(self.docs_dir, "*.md"))
        all_texts = []
        self.chunks = []
        for p in paths:
            with open(p, "r", encoding="utf-8") as f:
                raw = f.read()
            for c in self._chunk_text(raw):
                self.chunks.append(DocChunk(source=os.path.basename(p), text=c))
                all_texts.append(c)
        self.tfidf = self.vectorizer.fit_transform(all_texts) if all_texts else None

    def retrieve(self, query: str, topk: int = 3) -> List[DocChunk]:
        """检索与问题最相关的文档块。"""
        if not self.chunks or self.tfidf is None:
            return []
        qv = self.vectorizer.transform([query])
        sims = cosine_similarity(qv, self.tfidf).flatten()
        idxs = sims.argsort()[::-1][:topk]
        return [self.chunks[i] for i in idxs]
