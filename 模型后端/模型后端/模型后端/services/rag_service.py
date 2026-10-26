import chromadb
from services.embedding_service import get_embedding

client = chromadb.PersistentClient(path="./chroma_db")
collection = client.get_or_create_collection(name="fraud_cases")


def init_rag(cases: list):
    """
    初始化/写入向量库
    :param cases: [{"case_id": "...", "rag_text": "..."}]
    """
    print("正在更新向量数据库，请稍等...")
    documents = []
    ids = []
    embeddings = []

    for case in cases:
        case_id = case.get("case_id")
        text = case.get("rag_text")

        if not case_id or not text:
            continue

        documents.append(text)
        ids.append(case_id)

    # ⚠️ 批量 embedding（如果你的 get_embedding 不支持批量，可以先用单条）
    for text in documents:
        embeddings.append(get_embedding(text))

    # 👉 用 upsert 防重复（关键优化）
    collection.upsert(
        documents=documents,
        embeddings=embeddings,
        ids=ids
    )

    return len(ids)


# ===== 你原有代码（保持不变）=====
def retrieve(query, top_k=3):
    query_vec = get_embedding(query)

    results = collection.query(
        query_embeddings=[query_vec],
        n_results=top_k
    )

    return {
        "documents": results["documents"][0],
        "ids": results["ids"][0]
    }