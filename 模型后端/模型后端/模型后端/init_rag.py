from services.embedding_service import get_embedding
import chromadb

client = chromadb.PersistentClient(path="./chroma_db")
collection = client.get_or_create_collection(name="fraud_cases")

cases = [
    {"id": "fraud_001", "text": "诈骗分子建立股票投资群..."},
    {"id": "fraud_002", "text": "刷单诈骗，需要先垫付资金..."},
]

documents = [c["text"] for c in cases]
ids = [c["id"] for c in cases]

embeddings = [get_embedding(text) for text in documents]

collection.add(
    documents=documents,
    embeddings=embeddings,
    ids=ids
)

print("初始化完成")