(function() {
    const style = document.createElement('style');
    style.textContent = `
        #bm-chat-btn {
            position: fixed;
            bottom: 20px;
            right: 20px;
            width: 56px;
            height: 56px;
            border-radius: 50%;
            background: #007bff;
            color: white;
            border: none;
            font-size: 24px;
            cursor: pointer;
            box-shadow: 0 2px 10px rgba(0,0,0,0.2);
            z-index: 9999;
        }
        #bm-chat-panel {
            position: fixed;
            bottom: 90px;
            right: 20px;
            width: 300px;
            max-height: 420px;
            background: white;
            border-radius: 10px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.25);
            display: none;
            flex-direction: column;
            overflow: hidden;
            z-index: 9999;
            font-family: Arial, sans-serif;
        }
        #bm-chat-panel.open { display: flex; }
        #bm-chat-header {
            background: #007bff;
            color: white;
            padding: 10px 14px;
            font-size: 14px;
            font-weight: bold;
        }
        #bm-chat-messages {
            flex: 1;
            overflow-y: auto;
            padding: 10px;
            font-size: 13px;
            max-height: 300px;
        }
        .bm-msg {
            margin-bottom: 8px;
            padding: 6px 10px;
            border-radius: 10px;
            max-width: 80%;
            word-wrap: break-word;
        }
        .bm-msg.user {
            background: #007bff;
            color: white;
            margin-left: auto;
        }
        .bm-msg.bot {
            background: #f1f1f1;
            color: #333;
        }
        #bm-chat-input-row {
            display: flex;
            border-top: 1px solid #eee;
        }
        #bm-chat-input {
            flex: 1;
            border: none;
            padding: 10px;
            font-size: 13px;
        }
        #bm-chat-send {
            background: #007bff;
            color: white;
            border: none;
            padding: 0 14px;
            cursor: pointer;
        }
    `;
    document.head.appendChild(style);

    const btn = document.createElement('button');
    btn.id = 'bm-chat-btn';
    btn.textContent = '💬';
    document.body.appendChild(btn);

    const panel = document.createElement('div');
    panel.id = 'bm-chat-panel';
    panel.innerHTML = `
        <div id="bm-chat-header">BlueMart Assistant</div>
        <div id="bm-chat-messages"></div>
        <div id="bm-chat-input-row">
            <input id="bm-chat-input" type="text" placeholder="Ask a question..." maxlength="300">
            <button id="bm-chat-send">Send</button>
        </div>
    `;
    document.body.appendChild(panel);

    const messages = panel.querySelector('#bm-chat-messages');
    const input = panel.querySelector('#bm-chat-input');
    const sendBtn = panel.querySelector('#bm-chat-send');

    function addMessage(text, who) {
        const div = document.createElement('div');
        div.className = 'bm-msg ' + who;
        div.textContent = text;
        messages.appendChild(div);
        messages.scrollTop = messages.scrollHeight;
    }

    btn.addEventListener('click', () => {
        panel.classList.toggle('open');
        if (panel.classList.contains('open') && messages.children.length === 0) {
            addMessage("Hi! Ask me about orders, products, cart, reviews, or your account.", 'bot');
        }
    });

    async function sendMessage() {
        const text = input.value.trim();
        if (!text) return;
        addMessage(text, 'user');
        input.value = '';

        try {
            const response = await fetch('/bluemart/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: text })
            });
            const result = await response.json();
            addMessage(result.reply, 'bot');
        } catch (err) {
            addMessage("Sorry, something went wrong. Please try again.", 'bot');
        }
    }

    sendBtn.addEventListener('click', sendMessage);
    input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });
})();