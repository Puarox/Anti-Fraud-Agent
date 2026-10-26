"""Run one synthetic local investigation without external model calls."""

from jinjing.agent import Investigator, LocalQwenPlanner
from jinjing.cases import create_case


case = create_case("有人自称警察，要求我转账到安全账户，还给我一个陌生网址。")
with Investigator(LocalQwenPlanner()) as agent:
    result = agent.run(case)
print({
    "status": result["status"],
    "actions": [x["action"] for x in result["decisions"]],
    "tool_statuses": [x["status"] for x in result["tool_observations"]],
    "supported_hits": len(result["fraud_risk"]["supported_hits"]),
})
