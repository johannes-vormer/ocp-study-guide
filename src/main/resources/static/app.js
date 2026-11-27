// Syntax highlighting: markdown code blocks without a language are Java in this app.
document.addEventListener('DOMContentLoaded', () => {
    if (window.hljs) {
        document.querySelectorAll('pre code').forEach(el => {
            if (![...el.classList].some(c => c.startsWith('language-'))) el.classList.add('language-java');
            window.hljs.highlightElement(el);
        });
    }

    // Self-check checklists: "- [ ]" items in a doc become clickable and are saved per item position
    const docBody = document.getElementById('doc-body');
    if (docBody) {
        const boxes = [...docBody.querySelectorAll('input[type=checkbox]')];
        const saved = new Set((docBody.dataset.checked || '').split(',').filter(s => s !== '').map(Number));
        const count = document.getElementById('checklist-count');
        const bar = document.querySelector('.checklist-bar > span');
        const update = () => {
            const n = boxes.filter(b => b.checked).length;
            if (count) count.textContent = n;
            if (bar) bar.style.width = (boxes.length ? n * 100 / boxes.length : 0) + '%';
        };
        boxes.forEach((box, i) => {
            box.disabled = false;
            box.checked = saved.has(i);
            const li = box.closest('li');
            if (li) {
                li.classList.add('check-item');
                li.classList.toggle('checked', box.checked);
                // clicking the text toggles too
                li.addEventListener('click', e => { if (e.target !== box && !e.target.closest('a')) box.click(); });
            }
            box.addEventListener('change', async () => {
                li?.classList.toggle('checked', box.checked);
                update();
                const res = await fetch(`/study/${encodeURIComponent(docBody.dataset.doc)}/check`, {
                    method: 'POST', body: new URLSearchParams({ item: i, checked: box.checked }),
                }).catch(() => null);
                if (!res || !res.ok) { // roll back so the page never shows unsaved state
                    box.checked = !box.checked;
                    li?.classList.toggle('checked', box.checked);
                    update();
                    alert('Could not save — is the app still running?');
                }
            });
        });
        // anchor for the sidebar link: the heading just before the first checklist
        const firstList = boxes[0]?.closest('ul');
        let h = firstList?.previousElementSibling;
        while (h && !/^H[1-6]$/.test(h.tagName)) h = h.previousElementSibling;
        if (h) h.id = 'self-check';
        update();
    }

    // Exam countdown
    const timer = document.getElementById('timer');
    if (timer) {
        const end = Date.now() + Number(timer.dataset.seconds) * 1000;
        const tick = () => {
            const s = Math.max(0, Math.round((end - Date.now()) / 1000));
            timer.textContent = `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
            timer.classList.toggle('low', s < 600);
            if (s === 0) location.reload();
        };
        tick();
        setInterval(tick, 1000);
    }

    // Keyboard: A–H toggles options, Enter submits / goes to next question
    const form = document.getElementById('answer-form');
    document.addEventListener('keydown', e => {
        if (e.target.matches('input[type=text], input[type=date], textarea, select') || e.metaKey || e.ctrlKey || e.altKey) return;
        const next = document.getElementById('next-link');
        if (e.key === 'Enter') {
            if (next) { e.preventDefault(); location.href = next.href; }
            else if (form && form.querySelector('input[name=answer]:checked')) {
                e.preventDefault();
                form.querySelector('button[value=next]').click();
            }
            return;
        }
        const letter = e.key.toUpperCase();
        if (form && /^[A-H]$/.test(letter)) {
            const input = form.querySelector(`input[name=answer][value=${letter}]`);
            if (input && !input.disabled) input.type === 'radio' ? (input.checked = true) : (input.checked = !input.checked);
        }
    });
});
