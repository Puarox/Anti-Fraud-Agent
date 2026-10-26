import React, { useRef, useEffect, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Body1, Caption1, mergeClasses } from '@fluentui/react-components';

interface Message {
  role: string;
  content: string;
  time: string;
}

type CustomerInfo = {
  name: string;
  phone: string;
  status: string;
  balance: number;
  currentMonthCost: number;
  currentMonthBundleAmount: number;
  recentBillDate?: string | null;
  recentBillAmount?: number | null;
  unpaidBillAmount?: number | null;
};

interface ContextComponentProps {
  customerInfo: CustomerInfo;
  messages: Message[];
  agentAlias: string;
  styles: {
    contextList: string;
    contextTimestamp: string;
    contextRow: string;
    contextRowAgent: string;
    contextAvatar: string;
    contextAvatarCustomer: string;
    contextAvatarAgent: string;
    contextBubble: string;
    contextBubbleAgent: string;
    contextText: string;
  };
}

const isChatMessage = (msg: any): msg is Message => {
  return (
    typeof msg === 'object' &&
    msg !== null &&
    typeof msg.role === 'string' &&
    typeof msg.content === 'string' &&
    typeof msg.time === 'string'
  );
};

const ContextComponent: React.FC<ContextComponentProps> = ({ customerInfo, messages, agentAlias, styles }) => {
  const scrollRef = useRef<HTMLDivElement>(null);

  const visibleMessages = useMemo(() => messages.filter(isChatMessage), [messages]);
  const prevLenRef = useRef(visibleMessages.length);

  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;

    const currLen = visibleMessages.length;
    const prevLen = prevLenRef.current;

    if (currLen > prevLen) {
      el.scrollTop = el.scrollHeight;
    }

    prevLenRef.current = currLen;
  }, [visibleMessages.length]);

  const customerAvatar = (customerInfo.name || "客户").slice(0, 1);
  const agentAvatar = (agentAlias || "客服").slice(0, 1);

  return (
    <div ref={scrollRef} className={styles.contextList}  style={{
    overflowY: 'auto',
    overflowX: 'hidden', // ✅ 关键
    height: '100%',
  }}>
      <AnimatePresence initial={false}>
        {visibleMessages.map((msg, index) => {
          const isAgent = msg.role === 'AGENT' || msg.role === '客服';
          return (
            <motion.div
              key={msg.time + index}
              initial={{ opacity: 0, y: 20, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 10 }}
              transition={{ duration: 0.3, ease: "easeOut" }}
            >
              {/* 时间戳 */}
              <Caption1 className={styles.contextTimestamp}>
                {new Date(msg.time).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </Caption1>

              {/* 消息行 */}
              <div
                className={mergeClasses(
                  styles.contextRow,
                  isAgent && styles.contextRowAgent
                )}
              >
                {/* 头像 */}
                <div
                  className={mergeClasses(
                    styles.contextAvatar,
                    isAgent ? styles.contextAvatarAgent : styles.contextAvatarCustomer
                  )}
                >
                  {isAgent ? agentAvatar : customerAvatar}
                </div>

                {/* 气泡 */}
                <div
                  className={mergeClasses(
                    styles.contextBubble,
                    isAgent && styles.contextBubbleAgent
                  )}
                >
                  <Body1 className={styles.contextText}>{msg.content}</Body1>
                </div>
              </div>
            </motion.div>
          );
        })}
      </AnimatePresence>
    </div>
  );
};

export default ContextComponent;
