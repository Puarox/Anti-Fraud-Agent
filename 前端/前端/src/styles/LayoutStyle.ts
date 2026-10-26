import { makeStyles, tokens } from "@fluentui/react-components";

export const useStyles = makeStyles({
  root: {
    height: "100vh",
    display: "flex",
    flexDirection: "column",
    backgroundColor: tokens.colorNeutralBackground1,
    overflow: "hidden",
  },
  topBar: {
    height: "48px",
    backgroundColor: tokens.colorNeutralBackground4,
    borderBottom: `1px solid ${tokens.colorNeutralStroke2}`,
    display: "flex",
    alignItems: "center",
    gap: tokens.spacingHorizontalM,
    padding: `0 ${tokens.spacingHorizontalM}`,
    zIndex: 1000,
    flexShrink: 0,
    transition: "transform 0.3s ease-in-out",
  },
  topBarDashboard: {
    position: "fixed",
    top: 0,
    left: 0,
    right: 0,
    width: "100%",
    boxSizing: "border-box",
  },
  topBarHidden: {
    transform: "translateY(-100%)",
  },
  mainContainer: {
    display: "flex",
    flex: 1,
    overflow: "hidden",
    position: "relative",
  },
  content: {
    flex: "1",
    padding: "36px",
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalM,
    boxSizing: "border-box",
    overflowY: "auto",
    transition: "all 0.3s ease",
  },
  userSection: {
    display: "flex",
    alignItems: "center",
    gap: tokens.spacingHorizontalS,
    marginLeft: "auto",
  },
  username: {
    fontSize: tokens.fontSizeBase200,
    color: tokens.colorNeutralForeground1,
  },
  contentHiddenOnMobile: {
    "@media (max-width: 500px)": {
      display: "none",
    },
  },
});
