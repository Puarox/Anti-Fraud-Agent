import {
  mergeClasses,
  NavCategory,
  NavCategoryItem,
  NavDrawer,
  NavDrawerBody,
  NavDrawerFooter,
  NavItem,
  NavSubItem,
  NavSubItemGroup,
} from "@fluentui/react-components";
import {
  Apps20Color,
  Apps20Filled,
  AutoFitHeight20Regular,
  Board20Color,
  Board20Regular,
  bundleIcon,
  CheckboxPerson20Color,
  CheckboxPerson20Regular,
  
  DataPie20Color,
  DataPie20Regular,
  Document20Color,
  Document20Regular,
  Headset20Color,
  Headset20Filled,
  Home20Color,
  Home20Regular,
  PersonKey20Color,
  PersonKey20Regular,
  Settings20Color,
  Settings20Regular,
} from "@fluentui/react-icons";
import { useNavigate } from "react-router";
import { DrawerMotion, useStyles } from "../styles/NavigationViewStyle";
const CheckBoxPersonIcon = bundleIcon(CheckboxPerson20Color, CheckboxPerson20Regular);
const DocumentIcon = bundleIcon(Document20Color, Document20Regular);
const AutoEvolutionIcon = bundleIcon(Document20Color, AutoFitHeight20Regular);
const DashboardIcon = bundleIcon(Document20Color, AutoFitHeight20Regular);

interface INavigationProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  onItemClick?: () => void;
}

function getSelectedValue(): string {
  const path = location.pathname;
  switch (path) {
    case "/dashboard":
      return "1";
    case "/caseManagement":
      return "12";
    case "/userManagement":
      return "9";
    case "/autoEvolution":  // 自进化模块路由
      return "14";
    default:
      return "1";
  }
}

function getSelectedCategoryValue(): string {
  const path = location.pathname;
  switch (path) {
    case "/scriptConfiguration/processConfiguration":
    case "/scriptConfiguration/commonScriptConfiguration":
    case "/scriptConfiguration/feedbackScriptConfiguration":
      return "2";
    default:
      return "";
  }
}

export default function NavigationView({ isOpen, onOpenChange, onItemClick }: INavigationProps) {
  const styles = useStyles();
  const navigate = useNavigate();

  function handleNavigation(path: string) {
    navigate(path);
    onItemClick?.();
  }

  return (
    <NavDrawer
      selectedValue={getSelectedValue()}
      selectedCategoryValue={getSelectedCategoryValue()}
      open={isOpen}
      onOpenChange={(_, data) => onOpenChange(data.open)}
      surfaceMotion={{
        children: (_, props) => <DrawerMotion {...props} />,
      }}
      className={mergeClasses(styles.nav)}
      type="inline">
      <NavDrawerBody>
        <NavItem icon={<DashboardIcon />} value="1" onClick={() => handleNavigation("/dashboard")}>
          数据大屏
        </NavItem>
        <NavItem icon={<DocumentIcon />} value="12" onClick={() => handleNavigation("/caseManagement")}>
          案件管理
        </NavItem>
        <NavItem icon={<AutoEvolutionIcon />} value="14" onClick={() => handleNavigation("/autoEvolution")}>
          自进化模块
        </NavItem>
      </NavDrawerBody>
      <NavDrawerFooter className={styles.footer}>
        <NavItem icon={<CheckBoxPersonIcon />} value="9" onClick={() => handleNavigation("/userManagement")}>
          用户管理
        </NavItem>
      </NavDrawerFooter>
       <NavDrawerFooter className={styles.footer}>
        <NavItem icon={<CheckBoxPersonIcon />} value="10" onClick={() => handleNavigation("/settingsPage")}>
          设置
        </NavItem>
      </NavDrawerFooter>
    </NavDrawer>
  );
}