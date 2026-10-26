import { makeStyles, tokens } from "@fluentui/react-components";

export const useStyles = makeStyles({
  container: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalXL,
    maxWidth: "800px",
  },
  section: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalL,
  },
  sectionHeader: {
    display: "flex",
    alignItems: "center",
    gap: tokens.spacingHorizontalS,
  },
  sectionIcon: {
    color: tokens.colorBrandBackground,
  },
  themeCard: {
    padding: tokens.spacingVerticalL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusLarge,
    boxShadow: tokens.shadow4,
  },
  aboutCard: {
    padding: tokens.spacingVerticalL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusLarge,
    boxShadow: tokens.shadow4,
  },
  aboutContent: {
    display: "flex",
    alignItems: "center",
    gap: tokens.spacingHorizontalL,
    marginTop: tokens.spacingVerticalM,
  },
  logoContainer: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    width: "64px",
    height: "64px",
    backgroundColor: tokens.colorBrandBackground,
    borderRadius: tokens.borderRadiusLarge,
    color: "white",
    fontSize: "24px",
    fontWeight: "bold",
  },
  aboutInfo: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalXS,
    flex: 1,
  },
  versionInfo: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },
});
