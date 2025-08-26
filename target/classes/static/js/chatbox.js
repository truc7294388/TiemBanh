document.addEventListener("DOMContentLoaded", () => {
    const chatbotToggler = document.querySelector("#chatbot-toggler");
    const closeChatbot = document.querySelector("#close-chatbot");
    const chatbotPopup = document.querySelector(".chatbot-popup");
    const chatForm = document.querySelector(".chat-form");
    const chatBody = document.querySelector(".chat-body");
    const messageInput = document.querySelector(".message-input");
    const sendMessageButton = document.querySelector("#send-message");
    const fileInput = document.querySelector("#file-input");
    const fileUploadWrapper = document.querySelector(".file-upload-wrapper");
    const fileCancelButton = document.querySelector("#file-cancel");
    const emojiPickerButton = document.querySelector("#emoji-picker");
    const fileUploadBtn = document.querySelector("#file-upload");

    // ==========================
    // API setup
    // ==========================
    const API_KEY = "AIzaSyBxpaSr-b6nA3Ol2abrD-c0v3Xu-_DP0rE"; // thay bằng API key của bạn
    const API_URL = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${API_KEY}`;

    const chatHistory = [
        {
            role: "model",
            parts: [{
                text: `Bạn là chatbot tư vấn cho tiệm bánh ngọt "Sweet Home Bakery".
- Giới thiệu ngắn gọn, thân thiện.
- Menu gồm: Bánh kem 🎂, Bánh su kem 🍮, Bánh mì ngọt 🥐, Cupcake 🧁, Cookies 🍪, Trà sữa 🥤.
- Có nhận đặt bánh sinh nhật theo yêu cầu, giao hàng tận nơi tại Đà Nẵng.
- Khi khách hỏi giá thì trả lời cụ thể: bánh kem từ 120k, cupcake từ 15k/cái, cookies 10k/cái...
- Luôn xưng "Sweet Home" và nói chuyện thân thiện, lễ phép như nhân viên bán hàng.` }]
        }
    ];

    const userData = {
        message: null,
        file: { data: null, mime_type: null }
    };

    const initialInputHeight = messageInput.scrollHeight;

    // ==========================
    // Helper function
    // ==========================
    const createMessageElement = (content, ...classes) => {
        const div = document.createElement("div");
        div.classList.add("message", ...classes);
        div.innerHTML = content;
        return div;
    };

    // ==========================
    // Gọi API để sinh phản hồi
    // ==========================
    const generateBotResponse = async (incomingMessageDiv) => {
        const messageElement = incomingMessageDiv.querySelector(".message-text");

        chatHistory.push({
            role: "user",
            parts: [{ text: userData.message }, ...(userData.file.data ? [{ inline_data: userData.file }] : [])],
        });

        try {
            const response = await fetch(API_URL, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ contents: chatHistory })
            });

            const data = await response.json();
            if (!response.ok) throw new Error(data.error.message);

            const apiResponseText = data.candidates[0].content.parts[0].text.replace(/\*\*(.*?)\*\*/g, "$1").trim();
            messageElement.innerText = apiResponseText;

            chatHistory.push({ role: "model", parts: [{ text: apiResponseText }] });
        } catch (error) {
            messageElement.innerText = "Xin lỗi, hệ thống đang bị lỗi. Vui lòng thử lại!";
            messageElement.style.color = "#ff0000";
        } finally {
            userData.file = {};
            incomingMessageDiv.classList.remove("thinking");
            chatBody.scrollTo({ behavior: "smooth", top: chatBody.scrollHeight });
        }
    };

    // ==========================
    // Xử lý gửi tin nhắn
    // ==========================
    const handleOutgoingMessage = (e) => {
        e.preventDefault();
        userData.message = messageInput.value.trim();
        if (!userData.message && !userData.file.data) return;

        messageInput.value = "";
        fileUploadWrapper.classList.remove("file-uploaded");
        messageInput.dispatchEvent(new Event("input"));

        const messageContent = `
            <div class="message-text"></div>
            ${userData.file.data ? `<img src="data:${userData.file.mime_type};base64,${userData.file.data}" class="attachment" />` : ""}
        `;
        const outgoingMessageDiv = createMessageElement(messageContent, "user-message");
        outgoingMessageDiv.querySelector(".message-text").innerText = userData.message;
        chatBody.appendChild(outgoingMessageDiv);
        chatBody.scrollTop = chatBody.scrollHeight;

        setTimeout(() => {
            const messageContent = `
                <img class="bot-avatar" src="https://i.ibb.co/Y3Tz1pC/cute-cake.png" alt="Bot">
                <div class="message-text">
                    <div class="thinking-indicator"><div class="dot"></div><div class="dot"></div><div class="dot"></div></div>
                </div>`;
            const incomingMessageDiv = createMessageElement(messageContent, "bot-message", "thinking");
            chatBody.appendChild(incomingMessageDiv);
            chatBody.scrollTo({ behavior: "smooth", top: chatBody.scrollHeight });
            generateBotResponse(incomingMessageDiv);
        }, 600);
    };

    // ==========================
    // Event listeners
    // ==========================
    chatbotToggler.addEventListener("click", () => chatbotPopup.classList.toggle("active"));
    closeChatbot.addEventListener("click", () => chatbotPopup.classList.remove("active"));
    sendMessageButton.addEventListener("click", handleOutgoingMessage);
    chatForm.addEventListener("submit", handleOutgoingMessage);

    messageInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter" && !e.shiftKey && window.innerWidth > 768) {
            handleOutgoingMessage(e);
        }
    });

    messageInput.addEventListener("input", () => {
        messageInput.style.height = `${initialInputHeight}px`;
        messageInput.style.height = `${messageInput.scrollHeight}px`;
    });

    // ==========================
    // File upload
    // ==========================
    fileUploadBtn.addEventListener("click", () => fileInput.click());

    fileInput.addEventListener("change", (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const validImageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
        if (!validImageTypes.includes(file.type)) {
            alert("Chỉ chấp nhận file ảnh (JPEG, PNG, GIF, WEBP)");
            resetFileInput();
            return;
        }
        const reader = new FileReader();
        reader.onload = (e) => {
            fileUploadWrapper.querySelector("img").src = e.target.result;
            fileUploadWrapper.classList.add("file-uploaded");
            const base64String = e.target.result.split(",")[1];
            userData.file = { data: base64String, mime_type: file.type };
        };
        reader.readAsDataURL(file);
    });

    fileCancelButton.addEventListener("click", resetFileInput);

    function resetFileInput() {
        fileInput.value = "";
        fileUploadWrapper.classList.remove("file-uploaded");
        fileUploadWrapper.querySelector("img").src = "#";
        userData.file = { data: null, mime_type: null };
    }


    // ==========================
    // Emoji picker
    // ==========================
    if (emojiPickerButton) {
        const emojiContainer = document.createElement("div"); // ✅ phải dùng const
        emojiContainer.style.position = "absolute";
        emojiContainer.style.bottom = "60px";
        emojiContainer.style.right = "20px";
        emojiContainer.style.zIndex = "999";
        emojiContainer.style.display = "none";
        document.body.appendChild(emojiContainer);

        const picker = new EmojiMart.Picker({
            onEmojiSelect: (emoji) => {
                messageInput.value += emoji.native;
                messageInput.focus();
            },
            theme: "light",
            previewPosition: "none"
        });

        emojiContainer.appendChild(picker);

        // ✅ Sửa lại đúng tên biến
        emojiPickerButton.addEventListener("click", (e) => {
            e.stopPropagation(); // chặn click lan ra ngoài
            emojiContainer.style.display =
                emojiContainer.style.display === "none" ? "block" : "none";
        });

        document.addEventListener("click", (e) => {
            if (!emojiContainer.contains(e.target) && e.target !== emojiPickerButton) {
                emojiContainer.style.display = "none";
            }
        });
    }

});
