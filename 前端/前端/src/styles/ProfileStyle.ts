import { makeStyles, tokens } from "@fluentui/react-components";

export const useStyles = makeStyles({
  dialogContentSpacing: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalL,
  },
  container: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalXL,
    maxWidth: "800px",
  },
  header: {
    display: "flex",
    alignItems: "center",
    gap: tokens.spacingHorizontalXS,
  },
  section: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalL,
  },
  profileCard: {
    padding: tokens.spacingVerticalL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusLarge,
    boxShadow: tokens.shadow4,
  },
  avatarSection: {
    display: "flex",
    alignItems: "center",
    gap: tokens.spacingHorizontalL,
    marginBottom: tokens.spacingVerticalL,
    width: "100%",
    justifyContent: "space-between", // 让按钮靠右
  },
  avatarTextAndButton: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    flex: 1,
    width: "100%",
    gap: tokens.spacingHorizontalL,
  },
  avatarText: {
    flex: 1,
  },
  avatarButton: {
    marginLeft: "auto",
  },
  avatarContainer: {
    position: "relative",
  },
  uploadButton: {
    marginTop: tokens.spacingVerticalS,
  },
  infoRow: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: `${tokens.spacingVerticalXS} 0`,
  },
  infoLabel: {
    color: tokens.colorNeutralForeground2,
    minWidth: "90px",
  },
  infoValue: {
    flex: 1,
    marginLeft: tokens.spacingHorizontalM,
  },
  roleCheckboxes: {
    display: "flex",
    flexDirection: "column",
  },
  editButton: {
    minWidth: "auto",
  },
  sectionHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },
  phoneInputGroup: {
    display: "flex",
    gap: tokens.spacingHorizontalS,
    alignItems: "flex-start",
  },
  areaCodeSelect: {
    width: "180px",
  },
  phoneInput: {
    flex: 1,
  },
  dialogButtons: {
    display: "flex",
    gap: tokens.spacingHorizontalS,
  },
  avatarUploadArea: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingVerticalL,
    border: `2px dashed ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusMedium,
    textAlign: "center",
  },
});
