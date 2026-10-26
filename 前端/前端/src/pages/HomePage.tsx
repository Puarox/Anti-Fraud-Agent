import {
  Button,
  Card,
  Carousel,
  CarouselCard,
  CarouselNav,
  CarouselNavButton,
  CarouselNavContainer,
  CarouselSlider,
  CarouselViewport,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  DialogTrigger,
  Field,
  Image,
  Input,
  MessageBar,
  MessageBarBody,
  Textarea,
  Title1,
  Title3,
  tokens,
} from "@fluentui/react-components";
import { AddCircle24Regular, Checkmark16Regular, CheckmarkCircle16Regular, CheckmarkCircle24Filled, CheckmarkCircle24Regular } from "@fluentui/react-icons";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { useAuthStore } from "../stores/auth-store";
import { useStyles } from "../styles/HomeStyle";
import type { IHomeData } from "../types/IHomeData";
import api from "../utils/Api";
import { isAdmin } from "../utils/PermissionHelper";
import clsx from "clsx";

export default function HomePage() {
  const styles = useStyles();
  const ProfileData = useAuthStore.getState().profileData;
  const setProfileData = useAuthStore((state) => state.setProfileData);
  const alias = ProfileData?.alias || "用户";
  const navigate = useNavigate();

  const [notices] = useState<{ id: number; img: string }[]>([
    { id: 1, img: "/images/img_1.png" },
    { id: 2, img: "/images/img_2.png" },
    { id: 3, img: "/images/img_3.png" },
  ]);

  const [homeData, setHomeData] = useState<IHomeData>({
    pendingCalls: 0,
    completedCalls: 0,
    pendingCallbacks: 0,
    completedCallbacks: 0,
  });

  // 便笺数据
  const [notes, setNotes] = useState<{ memoName: string; content: string ;completed?: boolean;}[]>([]);

  // 获取便笺列表
  const fetchNotes = async () => {
    try {
      const response = await api.get("/memo/list",{
        headers:{
          userId: ProfileData?.id
        }
      });
      setNotes(response.data.data || []);
    } catch (error: any) {
      console.error("获取便笺列表失败:", error);
      setNotes([]);
    }
  };

  // 组件加载时获取便笺列表
  useEffect(() => {
    fetchNotes();
  }, []);

  // 新建便笺的表单状态
  const [newNoteInfo, setNewNoteInfo] = useState({
    memoName: "",
    content: "",
  });

  // 编辑便笺的表单状态
  const [editingNote, setEditingNote] = useState({
    memoName: "",
    content: "",
  });
  const toggleNoteStatus = (target: { memoName: string }) => {
  setNotes((prev) =>
    prev.map((note) =>
      note.memoName === target.memoName
        ? { ...note, completed: !note.completed }
        : note
    )
  );
};
  const isNumber = (str: string) => /^\d*$/.test(str);

  // 新建便笺对话框状态
  const [isNewDialogOpen, setIsNewDialogOpen] = useState(false);
  // 编辑便笺对话框状态
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);

  const [newNoteError, setNewNoteError] = useState("");
  const [editNoteError, setEditNoteError] = useState("");
  const [newTitleError, setNewTitleError] = useState<string | undefined>(undefined);
  const [newTextError, setNewTextError] = useState<string | undefined>(undefined);
  const [editTitleError, setEditTitleError] = useState<string | undefined>(undefined);
  const [editTextError, setEditTextError] = useState<string | undefined>(undefined);

  // 处理点击便笺项
  function handleNoteClick(note: { memoName: string; content: string }) {
    setEditingNote({
      memoName: note.memoName,
      content: note.content,
    });
    setEditTitleError(undefined);
    setEditTextError(undefined);
    setEditNoteError("");
    setIsEditDialogOpen(true);
  }

  // 处理新建便笺
  function handleNewNote() {
    setNewNoteInfo({
      memoName: "",
      content: "",
    });
    setNewTitleError(undefined);
    setNewTextError(undefined);
    setNewNoteError("");
    setIsNewDialogOpen(true);
  }

  // 处理保存新建便笺
  async function handleSaveNewNote() {
    // 表单验证
    let isValid = true;
    if (!newNoteInfo.memoName.trim()) {
      setNewTitleError("标题不能为空");
      isValid = false;
    } else {
      setNewTitleError(undefined);
    }
    if (!newNoteInfo.content.trim()) {
      setNewTextError("内容不能为空");
      isValid = false;
    } else {
      setNewTextError(undefined);
    }

    if (!isValid) return;

    const payload = {
      memoName: newNoteInfo.memoName,
      content: newNoteInfo.content,
    };
    try {
      await api.post("/memo/create", payload);
      // 刷新便笺列表
      fetchNotes();
      setIsNewDialogOpen(false);
      setNewNoteError("");
    } catch (error: any) {
      setNewNoteError(error.response?.data?.message || "保存失败，请稍后重试。");
    }
  }

  // 处理保存修改便笺
  async function handleSaveEditNote() {
    // 表单验证
    let isValid = true;
    if (!editingNote.memoName.trim()) {
      setEditTitleError("标题不能为空");
      isValid = false;
    } else {
      setEditTitleError(undefined);
    }
    if (!editingNote.content.trim()) {
      setEditTextError("内容不能为空");
      isValid = false;
    } else {
      setEditTextError(undefined);
    }

    if (!isValid) return;

    const payload = {
      memoName: editingNote.memoName,
      content: editingNote.content,
    };
    try {
      await api.put(`/memo/${editingNote.memoName}`, payload);
      // 刷新便笺列表
      fetchNotes();
      setIsEditDialogOpen(false);
      setEditNoteError("");
    } catch (error: any) {
      setEditNoteError(error.response?.data?.message || "保存失败，请稍后重试。");
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.welcomeSection}>
        <Title1>欢迎回来，{alias}！</Title1>
      </div>

      <div className={styles.mainContent}>
        {/* 通知公告 */}
        <Card className={styles.noticeSection}>
          <div className={styles.sectionHeader}>
            <Image src="/images/notice_title.svg" alt="通知公告" width={20} height={20} />
            <Title3>通知公告</Title3>
          </div>

          <Carousel draggable circular>
            <CarouselViewport>
              <CarouselSlider className={styles.slider}>
                {notices.map((notice, index) => (
                  <CarouselCard key={notice.id}  className={styles.wireframe}>
                    <Image
                      src={notice.img}
                      className={styles.noticeImage}
                      width="100%"
                      height="100%"
                      fit="cover"
                      loading="lazy"
                    />
                  </CarouselCard>
                ))}
              </CarouselSlider>
            </CarouselViewport>

            <CarouselNavContainer
              layout="inline"
              next={{ "aria-label": "go to next" }}
              prev={{ "aria-label": "go to prev" }}>
              <CarouselNav>{(index) => <CarouselNavButton aria-label={`Carousel Nav Button ${index}`} />}</CarouselNav>
            </CarouselNavContainer>
          </Carousel>
        </Card>

        {/* 左中右布局 */}
        <div className={styles.contentContainer}>
          <div className={styles.contentColumn}>
            <Card className={styles.statsSection}>
              <div className={styles.sectionHeader}>
                <Image src="/images/notes.svg" alt="便笺" width={25} height={25} />
                <Title3>便笺</Title3>
                <Dialog open={isNewDialogOpen} onOpenChange={(_, data) => setIsNewDialogOpen(data.open)}>
                  <DialogTrigger disableButtonEnhancement>

                    <Button
                      appearance="subtle"
                      icon={<AddCircle24Regular />}
                      style={{
                        position: "absolute",
                        right: "10px",
                      }}
                    >新增便笺</Button>

                  </DialogTrigger>
                  <DialogSurface>
                    <DialogBody>
                      <DialogTitle>新建便签</DialogTitle>
                      <DialogContent className={styles.dialogContentSpacing}>
                        {newNoteError && (
                          <MessageBar
                            intent="error"
                            style={{
                              marginBottom: tokens.spacingVerticalM,
                            }}>
                            <MessageBarBody>{newNoteError}</MessageBarBody>
                          </MessageBar>
                        )}

                        <Field
                          label="标题"
                          validationMessage={newTitleError}
                          validationState={newTitleError ? "error" : "none"}
                          size="large">
                          <Input
                            value={newNoteInfo.memoName}
                            onChange={(_, data) => {
                              setNewNoteInfo((prev) => ({
                                ...prev,
                                memoName: data.value,
                              }));
                              if (!data.value.trim()) {
                                setNewTitleError("标题不能为空");
                              } else {
                                setNewTitleError(undefined);
                              }
                            }}
                          />
                        </Field>

                        <Field
                          label="内容"
                          validationMessage={newTextError}
                          validationState={newTextError ? "error" : "none"}
                          size="large">
                          <Textarea
                            value={newNoteInfo.content}
                            rows={10}
                            onChange={(_, data) => {
                              setNewNoteInfo((prev) => ({
                                ...prev,
                                content: data.value,
                              }));
                              if (!data.value.trim()) {
                                setNewTextError("内容不能为空");
                              } else {
                                setNewTextError(undefined);
                              }
                            }}
                          />
                        </Field>
                      </DialogContent>
                      <DialogActions>
                        <Button appearance="secondary" onClick={() => setIsNewDialogOpen(false)}>
                          取消
                        </Button>
                        <Button
                          appearance="primary"
                          onClick={handleSaveNewNote}
                          disabled={!newNoteInfo.memoName.trim() || !newNoteInfo.content.trim()}>
                          保存
                        </Button>
                      </DialogActions>
                    </DialogBody>
                  </DialogSurface>
                </Dialog>
              </div>

              {/* 便笺列表 */}
              <div>
              {notes.map((note) => (
                <div
                  key={note.memoName}
                  className={styles.statNote}
                  onClick={() => handleNoteClick(note)}
                >
                  <div className={styles.memoContent}>
                    <div
                      className={`${styles.memoText} ${
                        note.completed ? styles.memoTextCompleted : ""
                      }`}
                    >
                      {note.memoName}
                    </div>
                    {/* 状态切换按钮 */}
                    
                    <div
                      className={`${styles.checkToggle} ${
                        note.completed ? styles.checkToggleActive : ""
                      }`}
                      onClick={(e) => {
                        e.stopPropagation();
                        toggleNoteStatus(note);
                      }}
                    >
                      {note.completed && (
                        <Checkmark16Regular className={styles.checkIcon} />
                      )}
                    </div>

                    {/* 文本 */}
                    
                  </div>
                </div>
              ))}
            </div>
            </Card>

            {/* 编辑便笺对话框 */}
            <Dialog open={isEditDialogOpen} onOpenChange={(_, data) => setIsEditDialogOpen(data.open)}>
              <DialogSurface>
                <DialogBody>
                  <DialogTitle>编辑便笺</DialogTitle>
                  <DialogContent className={styles.dialogContentSpacing}>
                    {editNoteError && (
                      <MessageBar
                        intent="error"
                        style={{
                          marginBottom: tokens.spacingVerticalM,
                        }}>
                        <MessageBarBody>{editNoteError}</MessageBarBody>
                      </MessageBar>
                    )}

                    <Field
                      label="标题"
                      validationMessage={editTitleError}
                      validationState={editTitleError ? "error" : "none"}
                      size="large">
                      <Input
                        value={editingNote.memoName}
                        onChange={(_, data) => {
                          setEditingNote((prev) => ({
                            ...prev,
                            memoName: data.value,
                          }));
                          if (!data.value.trim()) {
                            setEditTitleError("标题不能为空");
                          } else {
                            setEditTitleError(undefined);
                          }
                        }}
                      />
                    </Field>

                    <Field
                      label="内容"
                      validationMessage={editTextError}
                      validationState={editTextError ? "error" : "none"}
                      size="large">
                      <Textarea
                        value={editingNote.content}
                        rows={10}
                        onChange={(_, data) => {
                          setEditingNote((prev) => ({
                            ...prev,
                            content: data.value,
                          }));
                          if (!data.value.trim()) {
                            setEditTextError("内容不能为空");
                          } else {
                            setEditTextError(undefined);
                          }
                        }}
                      />
                    </Field>
                  </DialogContent>
                  <DialogActions>
                    <Button appearance="secondary" onClick={() => setIsEditDialogOpen(false)}>
                      取消
                    </Button>
                    <Button
                      appearance="primary"
                      onClick={handleSaveEditNote}
                      disabled={!editingNote.memoName.trim() || !editingNote.content.trim()}>
                      保存
                    </Button>
                  </DialogActions>
                </DialogBody>
              </DialogSurface>
            </Dialog>
          </div>
          <div className={styles.contentColumn}>
            {/* 我的KPI */}
            <Card className={styles.statsSection}>
              <div className={styles.sectionHeader}>
                <Image src="/images/my_kpi_title.svg" alt="我的KPI" width={20} height={20} />
                <Title3>我的KPI</Title3>
              </div>
              <div className={styles.statsGrid}>
                {isAdmin() && (
                  <>
                    <div className={styles.statCard}>
                      <Image src="/images/call_answered.svg" alt="待接听电话数" className={styles.statImage} />
                      <div className={styles.statContent}>
                        <div className={styles.statNumber}>4.63/5</div>
                        <div className={styles.statLabel}>用户综合评分</div>
                      </div>
                    </div>
                    <div className={styles.statCard}>
                      <Image src="/images/not_call_back.svg" alt="待回拨电话数" className={styles.statImage} />
                      <div className={styles.statContent}>
                        <div className={styles.statNumber}>93</div>
                        <div className={styles.statLabel}>最近七天业务数量</div>
                      </div>
                    </div>
                  </>
                )}
              </div>
            </Card>
          </div>
          <div className={styles.contentColumn}>
            {/* 我的已办 */}
            <Card className={styles.statsSection}>
              <div className={styles.sectionHeader}>
                <Image src="/images/my_done_title.svg" alt="我的已办" style={{ width: "20px", height: "20px" }} />
                <Title3>我的已办</Title3>
              </div>
              <div className={styles.statsGrid}>
                {isAdmin() && (
                  <>
                    <div className={styles.statCard}>
                      <Image src="/images/call_answered.svg" alt="已接听电话数" className={styles.statImage} />
                      <div className={styles.statContent}>
                        <div className={styles.statNumber}>{homeData.completedCalls}</div>
                        <div className={styles.statLabel}>已接听电话数</div>
                      </div>
                    </div>
                    <div className={styles.statCard}>
                      <Image src="/images/call_back.svg" alt="已回拨电话数" className={styles.statImage} />
                      <div className={styles.statContent}>
                        <div className={styles.statNumber}>{homeData.completedCallbacks}</div>
                        <div className={styles.statLabel}>已回拨电话数</div>
                      </div>
                    </div>
                  </>
                )}
              </div>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
