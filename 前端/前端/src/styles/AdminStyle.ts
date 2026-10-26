import { makeStyles, tokens } from "@fluentui/react-components";

export const useStyles = makeStyles({
  container: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalL,
    maxWidth: "100vw",
    flex: 1,
    height: "100%",
  },
  mainTabs: {
    marginLeft: "-12px",
    marginBottom: tokens.spacingVerticalL,
  },
  subTabs: {
    marginBottom: tokens.spacingVerticalM,
  },
  toolbar: {
    display: "flex",
    flexWrap: "wrap",
    gap: tokens.spacingHorizontalM,
    alignItems: "center",
    marginBottom: tokens.spacingVerticalM,
    padding: tokens.spacingVerticalM,
    backgroundColor: tokens.colorNeutralBackground2,
    borderRadius: tokens.borderRadiusMedium,
  },
  searchBox: {
    minWidth: "250px",
  },
  datePickerGroup: {
    display: "flex",
    gap: tokens.spacingHorizontalS,
    alignItems: "center",
  },
  checkboxGroup: {
    display: "flex",
    alignItems: "center",
  },
  table: {
    minWidth: "800px",
    marginBottom: tokens.spacingVerticalM,
  },
  oddRow: {
    backgroundColor: tokens.colorNeutralBackground1,
  },
  evenRow: {
    backgroundColor: tokens.colorNeutralBackground2,
  },
  pagination: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    gap: tokens.spacingHorizontalM,
    marginTop: "auto",
    paddingTop: tokens.spacingVerticalM,
  },
  roleCheckboxes: {
    display: "flex",
    flexDirection: "column",
  },
  buttonGroup: {
    display: "flex",
    gap: tokens.spacingHorizontalS,
  },
  viewDialogContent: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalM,
    marginTop: tokens.spacingVerticalM,
  },
  loadingContainer: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    height: "200px",
  },
  errorMessage: {
    marginBottom: tokens.spacingVerticalM,
  },
  spinnerContainer: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    height: "200px",
  },
  tableContainer: {
    position: "relative",
    display: "flex",
    flexDirection: "column",
    flex: 1,
    minHeight: "200px",
  },
});
