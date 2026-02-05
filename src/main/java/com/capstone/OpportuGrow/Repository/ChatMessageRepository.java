package com.capstone.OpportuGrow.Repository;



import com.capstone.OpportuGrow.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;




public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // جلب كل الرسائل التي وصلت لشخص معين مرتبة من الأحدث
    List<ChatMessage> findByReceiverIdOrderByTimestampDesc(Long receiverId);

    // جلب التاريخ الكامل للمحادثة بين طرفين (لصفحة الشات)
    @Query("""
        SELECT m FROM ChatMessage m 
        WHERE (m.sender.id = :u1 AND m.receiver.id = :u2) 
        OR (m.sender.id = :u2 AND m.receiver.id = :u1) 
        ORDER BY m.timestamp ASC
    """)
    List<ChatMessage> findChatHistory(@Param("u1") Long u1, @Param("u2") Long u2);
    @Query(value = "SELECT * FROM chat_messages WHERE id IN " +
            "(SELECT MAX(id) FROM chat_messages WHERE receiver_id = :adminId " +
            "GROUP BY sender_id)", nativeQuery = true)
    List<ChatMessage> findRecentMessagesForAdmin(@Param("adminId") Long adminId);
}