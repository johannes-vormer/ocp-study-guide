// Syntax highlighting: markdown code blocks without a language are Java in this app.
document.addEventListener('DOMContentLoaded', () => {
    if (window.hljs) {
        document.querySelectorAll('pre code').forEach(el => {
            if (![...el.classList].some(c => c.startsWith('language-'))) el.classList.add('language-java');
            window.hljs.highlightElement(el);
        });
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
