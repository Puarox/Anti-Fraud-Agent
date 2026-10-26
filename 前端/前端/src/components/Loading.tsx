import { Spinner } from "@fluentui/react-components";
import { useStyles } from "../styles/LoadingStyle";

export default function Loading() {
  const styles = useStyles();
  return (
    <div className={styles.container}>
      <Spinner size="huge" />
    </div>
  );
}
