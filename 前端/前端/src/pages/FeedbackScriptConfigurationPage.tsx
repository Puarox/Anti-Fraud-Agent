import ScriptConfigurationView from "../components/ScriptConfigurationView";

export default function FeedbackScriptConfigurationPage() {
  return (
    <ScriptConfigurationView
      title="反馈话术配置"
      scriptType="FEEDBACK"
      listApiEndpoint="/script/list/feedback"
      defaultAssociateId="READ"
      showOperationType={true}
    />
  );
}
