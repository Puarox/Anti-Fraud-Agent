import ScriptConfigurationView from "../components/ScriptConfigurationView";

export default function CommonScriptConfigurationPage() {
  return (
    <ScriptConfigurationView
      title="通用话术配置"
      scriptType="COMMON"
      listApiEndpoint="/script/list/common"
      showOperationType={false}
    />
  );
}
