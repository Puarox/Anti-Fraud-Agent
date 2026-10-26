import {
  Avatar,
  Body1,
  Button,
  Card,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  DialogTrigger,
  Dropdown,
  Field,
  Input,
  Label,
  MessageBar,
  MessageBarBody,
  Option,
  Title1,
  Title3,
  tokens,
} from "@fluentui/react-components";
import { Delete20Regular, Edit20Regular, Image20Regular } from "@fluentui/react-icons";
import { useEffect, useState } from "react";
import { AreaCodes } from "../models/AreaCode";
import { useAuthStore } from "../stores/auth-store";
import { useStyles } from "../styles/ProfileStyle";
import api from "../utils/Api";
import { hashPassword } from "../utils/PasswordHasher";
import { PermissionCodeToTextConverter } from "../utils/PermissionHelper";

export default function ProfilePage() {
  const token = useAuthStore((state) => state.token);
  const ProfileData = useAuthStore((state) => state.profileData);
  const setProfileData = useAuthStore((state) => state.setProfileData);

  // 校验状态
  const [avatarEditError, setAvatarEditError] = useState("");
  const [infoEditError, setInfoEditError] = useState("");
  const [passwordEditError, setPasswordEditError] = useState<string | undefined>("");
  const [aliasError, setAliasError] = useState<string | undefined>(undefined);
  const [emailError, setEmailError] = useState<string | undefined>(undefined);
  const [phoneError, setPhoneError] = useState<string | undefined>(undefined);
  const [passwordError, setPasswordError] = useState<string | undefined>(undefined);
  const styles = useStyles();

  useEffect(() => {
    fetchUserDetail();
  }, [token, setProfileData]);

  // 状态管理 - 所有 hooks 必须在条件返回之前
  const [areaCode, setAreaCode] = useState(ProfileData?.phone?.split(" ")[0] || "+86");
  const [phone, setPhone] = useState(ProfileData?.phone?.split(" ")[1] || "");

  // 对话框状态
  const [isAvatarDialogOpen, setIsAvatarDialogOpen] = useState(false);
  const [isInfoDialogOpen, setIsInfoDialogOpen] = useState(false);
  const [isPasswordDialogOpen, setIsPasswordDialogOpen] = useState(false);

  // 表单状态
  const [editingInfo, setEditingInfo] = useState({
    alias: ProfileData?.alias || "",
    email: ProfileData?.email || "",
    areaCode: ProfileData?.phone?.split(" ")[0] || "+86",
    phoneNumber: ProfileData?.phone?.split(" ")[1] || "",
  });
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  // 当 ProfileData 更新时，同步更新相关状态
  useEffect(() => {
    if (ProfileData) {
      const phoneParts = ProfileData.phone?.split(" ") || [];
      setAreaCode(phoneParts[0] || "+86");
      setPhone(phoneParts[1] || "");
      setEditingInfo({
        alias: ProfileData.alias || "",
        email: ProfileData.email || "",
        areaCode: phoneParts[0] || "+86",
        phoneNumber: phoneParts[1] || "",
      });
    }
  }, [ProfileData]);

  // 如果 ProfileData 为空，显示加载状态或返回
  if (!ProfileData) {
    return (
      <div className={styles.container}>
        <Title1>个人中心</Title1>
        <Body1>加载中...</Body1>
      </div>
    );
  }

  // 文件验证配置
  const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
  const ALLOWED_IMAGE_TYPES = ["image/jpeg", "image/jpg", "image/png"];

  // 验证文件的函数
  const validateFile = (file: File): string | null => {
    // 检查文件类型
    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
      return "只支持 JPEG、PNG 格式的图片文件";
    }

    // 检查文件大小
    if (file.size > MAX_FILE_SIZE) {
      return `文件大小不能超过 ${MAX_FILE_SIZE / (1024 * 1024)}MB`;
    }

    return null; // 验证通过
  };

  async function fetchUserDetail() {
    if (!token) return;
    try {
      const res = await api.get("/user/getUserDetail");
      if (res.data?.success && res.data?.data) {
        setProfileData(res.data.data);
        console.log("获取用户详情成功，", ProfileData);
        return true;
      }
    } catch (error) {
      console.error("获取用户详情失败，", error);
    }
  }

  // 头像上传处理
  function handleAvatarUpload() {
    const input = document.createElement("input");
    input.type = "file";
    input.accept = "image/jpeg,image/jpg,image/png";
    input.onchange = async (e) => {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (file) {
        setAvatarEditError("");
        const validationError = validateFile(file);
        if (validationError) {
          setAvatarEditError(validationError);
          return;
        }

        // 1. 上传图片到后端
        const formData = new FormData();
        formData.append("file", file);

        try {
          const res = await api.post("/user/uploadAvatar", formData, {
            headers: {
              "Content-Type": "multipart/form-data",
            },
          });
          if (!res.data?.success) {
            setAvatarEditError("上传失败，未返回头像地址。");
            return;
          }

          fetchUserDetail();
          setIsAvatarDialogOpen(false);
          setAvatarEditError("");
        } catch (error: any) {
          setAvatarEditError(error.response?.data?.message || "头像上传失败，请重试。");
        }
      }
    };
    input.click();
  }

  async function handleClearAvatar() {
    try {
      await api.put("/user/updateUserDetail", {
        ...ProfileData,
        avatarUrl: "",
      });
      // 更新本地状态并关闭对话框
      setProfileData({ ...ProfileData, avatarUrl: undefined } as any);
      setIsAvatarDialogOpen(false);
      setAvatarEditError("");
    } catch (error: any) {
      setAvatarEditError(error.response?.data?.message || "头像清除失败，请重试。");
    }
  }

  // 校验辅助函数
  function validateEmail(email: string) {
    // 简单邮箱正则
    return /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email);
  }

  function isNumber(str: string) {
    return /^\d*$/.test(str);
  }

  async function handleSaveInfo() {
    const payload = {
      alias: editingInfo.alias || "",
      email: editingInfo.email,
      phone: editingInfo.phoneNumber ? `${editingInfo.areaCode} ${editingInfo.phoneNumber}` : "",
      avatarUrl: ProfileData?.avatarUrl,
    };
    try {
      await api.put("/user/updateUserDetail", payload);
      // 更新本地状态
      setProfileData({
        ...ProfileData,
        ...payload,
      } as any);
      setIsInfoDialogOpen(false);
    } catch (error: any) {
      setInfoEditError(error.response?.data?.message || "保存失败，请稍后重试。");
    }
  }

  async function handleSavePassword() {
    setPasswordError(undefined);
    setPasswordEditError("");
    try {
      // hash 密码
      const oldPasswordHash = await hashPassword(passwordForm.oldPassword);
      const newPasswordHash = await hashPassword(passwordForm.newPassword);
      console.log(oldPasswordHash, newPasswordHash);
      // 请求
      await api.put(`/user/password`, null, {
        params: {
          oldPassword: oldPasswordHash,
          newPassword: newPasswordHash,
        },
      });
      setIsPasswordDialogOpen(false);
      setPasswordForm({
        oldPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    } catch (error: any) {
      setPasswordEditError(error.response?.data?.message || "修改密码失败，请重试。");
    }
  }

  return (
    <div className={styles.container}>
      <Title1>个人中心</Title1>

      {/* 头像和基本信息 */}
      <Card className={styles.profileCard}>
        <div className={styles.avatarSection}>
          <div className={styles.avatarContainer}>
            <Avatar
              size={120}
              name={ProfileData.username}
              image={ProfileData.avatarUrl ? { src: ProfileData.avatarUrl } : undefined}
            />
          </div>
          <div className={styles.avatarTextAndButton}>
            <Body1 className={styles.avatarText}>个性化您的账户。</Body1>
            <Dialog
              open={isAvatarDialogOpen}
              onOpenChange={(_, data) => {
                setIsAvatarDialogOpen(data.open);
                // 关闭对话框时清除错误消息
                if (!data.open) {
                  setAvatarEditError("");
                }
              }}>
              <DialogTrigger disableButtonEnhancement>
                <Button appearance="primary" className={styles.avatarButton}>
                  修改头像
                </Button>
              </DialogTrigger>
              <DialogSurface>
                <DialogBody>
                  <DialogTitle>修改头像</DialogTitle>
                  <DialogContent className={styles.dialogContentSpacing}>
                    {/* 显示错误消息 */}
                    {avatarEditError && (
                      <MessageBar
                        intent="error"
                        style={{
                          marginBottom: tokens.spacingVerticalM,
                        }}>
                        <MessageBarBody>{avatarEditError}</MessageBarBody>
                      </MessageBar>
                    )}

                    <div className={styles.avatarUploadArea}>
                      <Avatar
                        size={72}
                        name={ProfileData.username}
                        image={
                          ProfileData.avatarUrl
                            ? {
                                src: ProfileData.avatarUrl,
                              }
                            : undefined
                        }
                      />

                      {/* 文件要求说明 */}
                      <Body1
                        style={{
                          color: tokens.colorNeutralForeground2,
                          fontSize: tokens.fontSizeBase200,
                        }}>
                        支持 JPEG、PNG 格式
                        <br />
                        文件大小不超过 5MB
                      </Body1>

                      <div className={styles.dialogButtons}>
                        <Button appearance="secondary" icon={<Delete20Regular />} onClick={handleClearAvatar}>
                          清除头像
                        </Button>
                        <Button appearance="primary" icon={<Image20Regular />} onClick={handleAvatarUpload}>
                          上传头像
                        </Button>
                      </div>
                    </div>
                  </DialogContent>
                  <DialogActions>
                    <DialogTrigger disableButtonEnhancement>
                      <Button appearance="secondary">取消</Button>
                    </DialogTrigger>
                    <DialogTrigger disableButtonEnhancement>
                      <Button appearance="primary">完成</Button>
                    </DialogTrigger>
                  </DialogActions>
                </DialogBody>
              </DialogSurface>
            </Dialog>
          </div>
        </div>

        {/* 基本信息 */}
        <div className={styles.infoRow}>
          <Label className={styles.infoLabel}>全名</Label>
          <Body1 className={styles.infoValue}>{ProfileData.username}</Body1>
        </div>
      </Card>

      {/* 个人资料信息 */}
      <Card className={styles.profileCard}>
        <div className={styles.sectionHeader}>
          <Title3>个人资料信息</Title3>
          <Dialog open={isInfoDialogOpen} onOpenChange={(_, data) => setIsInfoDialogOpen(data.open)}>
            <DialogTrigger disableButtonEnhancement>
              <Button appearance="primary" icon={<Edit20Regular />}>
                编辑个人资料信息
              </Button>
            </DialogTrigger>
            <DialogSurface>
              <DialogBody>
                <DialogTitle>编辑个人资料信息</DialogTitle>
                <DialogContent className={styles.dialogContentSpacing}>
                  {infoEditError && (
                    <MessageBar
                      intent="error"
                      style={{
                        marginBottom: tokens.spacingVerticalM,
                      }}>
                      <MessageBarBody>{infoEditError}</MessageBarBody>
                    </MessageBar>
                  )}

                  <Field label="昵称" validationMessage={aliasError} validationState={aliasError ? "error" : "none"}>
                    <Input
                      value={editingInfo.alias}
                      maxLength={50}
                      onChange={(_, data) => {
                        setEditingInfo((prev) => ({
                          ...prev,
                          alias: data.value,
                        }));
                        if (!data.value || data.value.length > 50) {
                          setAliasError("昵称不能为空且不能超过50字符");
                        } else {
                          setAliasError(undefined);
                        }
                      }}
                    />
                  </Field>

                  <Field label="邮箱" validationMessage={emailError} validationState={emailError ? "error" : "none"}>
                    <Input
                      type="email"
                      value={editingInfo.email}
                      maxLength={255}
                      onChange={(_, data) => {
                        setEditingInfo((prev) => ({
                          ...prev,
                          email: data.value,
                        }));
                        if (data.value && !validateEmail(data.value)) {
                          setEmailError("邮箱格式不正确");
                        } else {
                          setEmailError(undefined);
                        }
                      }}
                    />
                  </Field>

                  <Field label="电话" validationMessage={phoneError} validationState={phoneError ? "error" : "none"}>
                    <div className={styles.phoneInputGroup}>
                      <Dropdown
                        className={styles.areaCodeSelect}
                        value={AreaCodes.find((c) => c.key === editingInfo.areaCode)?.text || editingInfo.areaCode}
                        selectedOptions={[editingInfo.areaCode]}
                        onOptionSelect={(_, data) =>
                          setEditingInfo((prev) => ({
                            ...prev,
                            areaCode: data.optionValue || prev.areaCode,
                          }))
                        }>
                        {AreaCodes.map((code) => (
                          <Option key={code.key} value={code.key}>
                            {code.text}
                          </Option>
                        ))}
                      </Dropdown>
                      <Input
                        className={styles.phoneInput}
                        value={editingInfo.phoneNumber}
                        maxLength={11}
                        onChange={(_, data) => {
                          // 只允许数字
                          if (!data.value || isNumber(data.value)) {
                            setEditingInfo((prev) => ({
                              ...prev,
                              phoneNumber: data.value,
                            }));
                            setPhoneError(undefined);
                          }
                        }}
                      />
                    </div>
                  </Field>
                </DialogContent>
                <DialogActions>
                  <DialogTrigger disableButtonEnhancement>
                    <Button appearance="secondary">取消</Button>
                  </DialogTrigger>
                  <Button
                    appearance="primary"
                    onClick={handleSaveInfo}
                    disabled={!editingInfo.alias || (!!editingInfo.email && !validateEmail(editingInfo.email))}>
                    保存
                  </Button>
                </DialogActions>
              </DialogBody>
            </DialogSurface>
          </Dialog>
        </div>

        <div className={styles.infoRow}>
          <Label className={styles.infoLabel}>昵称</Label>
          <Body1 className={styles.infoValue}>{ProfileData.alias}</Body1>
        </div>

        <div className={styles.infoRow}>
          <Label className={styles.infoLabel}>邮箱</Label>
          <Body1 className={styles.infoValue}>{ProfileData.email}</Body1>
        </div>

        <div className={styles.infoRow}>
          <Label className={styles.infoLabel}>电话</Label>
          <Body1 className={styles.infoValue}>{ProfileData.phone}</Body1>
        </div>
      </Card>

      {/* 账户信息 */}
      <Card className={styles.profileCard}>
        <div className={styles.sectionHeader}>
          <Title3>账户信息</Title3>
          <Dialog open={isPasswordDialogOpen} onOpenChange={(_, data) => setIsPasswordDialogOpen(data.open)}>
            <DialogTrigger disableButtonEnhancement>
              <Button appearance="primary" icon={<Edit20Regular />}>
                修改我的密码
              </Button>
            </DialogTrigger>
            <DialogSurface>
              <DialogBody>
                <DialogTitle>修改我的密码</DialogTitle>
                <DialogContent className={styles.dialogContentSpacing}>
                  {passwordEditError && (
                    <MessageBar
                      intent="error"
                      style={{
                        marginBottom: tokens.spacingVerticalM,
                      }}>
                      <MessageBarBody>{passwordEditError}</MessageBarBody>
                    </MessageBar>
                  )}
                  <Field label="当前密码" required>
                    <Input
                      type="password"
                      value={passwordForm.oldPassword}
                      onChange={(_, data) =>
                        setPasswordForm((prev) => ({
                          ...prev,
                          oldPassword: data.value,
                        }))
                      }
                    />
                  </Field>

                  <Field
                    label="新密码"
                    required
                    validationMessage={passwordError}
                    validationState={passwordError ? "error" : "none"}>
                    <Input
                      type="password"
                      value={passwordForm.newPassword}
                      maxLength={50}
                      onChange={(_, data) => {
                        setPasswordForm((prev) => ({
                          ...prev,
                          newPassword: data.value,
                        }));
                        if (!data.value || data.value.length > 50) {
                          setPasswordError("新密码不能为空且不能超过50字符");
                        } else {
                          setPasswordError(undefined);
                        }
                      }}
                    />
                  </Field>

                  <Field
                    label="确认新密码"
                    required
                    validationMessage={
                      passwordForm.newPassword !== passwordForm.confirmPassword ? "密码不匹配" : undefined
                    }
                    validationState={passwordForm.newPassword !== passwordForm.confirmPassword ? "error" : "none"}>
                    <Input
                      type="password"
                      value={passwordForm.confirmPassword}
                      maxLength={50}
                      onChange={(_, data) =>
                        setPasswordForm((prev) => ({
                          ...prev,
                          confirmPassword: data.value,
                        }))
                      }
                    />
                  </Field>
                </DialogContent>
                <DialogActions>
                  <DialogTrigger disableButtonEnhancement>
                    <Button appearance="secondary">取消</Button>
                  </DialogTrigger>
                  <Button
                    appearance="primary"
                    onClick={handleSavePassword}
                    disabled={
                      !passwordForm.oldPassword ||
                      !passwordForm.newPassword ||
                      passwordForm.newPassword !== passwordForm.confirmPassword
                    }>
                    保存
                  </Button>
                </DialogActions>
              </DialogBody>
            </DialogSurface>
          </Dialog>
        </div>

        <div className={styles.infoRow}>
          <Label className={styles.infoLabel}>权限</Label>
          <Body1 className={styles.infoValue}>{PermissionCodeToTextConverter(ProfileData.privilege)}</Body1>
        </div>
      </Card>
    </div>
  );
}
