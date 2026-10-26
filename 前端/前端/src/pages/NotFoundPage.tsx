import { Button, Text } from "@fluentui/react-components";
import { ArrowLeft24Regular, Home24Regular } from "@fluentui/react-icons";
import { useNavigate } from "react-router";
import { useStyles } from "../styles/NotFoundStyle";

export default function NotFoundPage() {
  const styles = useStyles();
  const navigate = useNavigate();

  return (
    <div className={styles.root}>
      <div className={styles.errorCode}>404</div>
      <Text className={styles.title}>页面未找到</Text>
      <Text className={styles.description}>抱歉，您访问的页面不存在。它可能已被移动、删除，或者您输入的网址有误。</Text>
      <div className={styles.buttonContainer}>
        <Button appearance="secondary" icon={<ArrowLeft24Regular />} onClick={() => navigate(-1)}>
          返回上一页
        </Button>
        <Button appearance="primary" icon={<Home24Regular />} onClick={() => navigate("/")}>
          回到首页
        </Button>
      </div>
    </div>
  );
}
