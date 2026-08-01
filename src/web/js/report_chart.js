/**
 * 📈 LibraAI Report Analytics Canvas Renderer
 */
document.addEventListener("DOMContentLoaded", function () {
    const canvas = document.getElementById("analyticsCanvas");
    if (!canvas) return;

    function drawChart() {
        // Set internal resolution based on parent container size
        const parent = canvas.parentNode;
        if (!parent) return;
        const rect = parent.getBoundingClientRect();
        
        // Dynamically compute width to fit parent container width, up to 600px
        canvas.width = Math.max(280, Math.min(600, rect.width - 20));
        canvas.height = 250;

        const ctx = canvas.getContext("2d");
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Sample Data
        const data = [
            { label: "Computer Sci", value: 45, color: "#F97316" },
            { label: "Data Science", value: 30, color: "#3B82F6" },
            { label: "Physics", value: 20, color: "#22C55E" },
            { label: "Math", value: 25, color: "#7C3AED" },
            { label: "Literature", value: 15, color: "#F59E0B" }
        ];

        const startX = Math.min(60, canvas.width * 0.1);
        const startY = 200;
        const chartHeight = 130;
        const maxValue = 50;
        
        // Calculate dynamic bar width and gap to fit canvas width
        const availableWidth = canvas.width - startX - 30;
        const barWidth = Math.min(50, Math.floor(availableWidth / data.length) * 0.6);
        const gap = Math.floor((availableWidth - (data.length * barWidth)) / (data.length - 1));

        // Draw Axis
        ctx.beginPath();
        ctx.strokeStyle = "#E5E7EB";
        ctx.lineWidth = 2;
        ctx.moveTo(startX - 10, startY);
        ctx.lineTo(startX + (data.length * (barWidth + gap)) - gap + 10, startY);
        ctx.stroke();

        // Render Bars
        data.forEach((item, index) => {
            const x = startX + index * (barWidth + gap);
            const barH = (item.value / maxValue) * chartHeight;
            const y = startY - barH;

            // Draw Bar
            ctx.fillStyle = item.color;
            ctx.beginPath();
            ctx.roundRect(x, y, barWidth, barH, [6, 6, 0, 0]);
            ctx.fill();

            // Draw Text Value
            ctx.fillStyle = "#1F2937";
            ctx.font = "bold 11px Segoe UI";
            ctx.textAlign = "center";
            ctx.fillText(item.value, x + barWidth / 2, y - 8);

            // Draw Label
            ctx.fillStyle = "#6B7280";
            ctx.font = "10px Segoe UI";
            ctx.fillText(item.label.substring(0, 10), x + barWidth / 2, startY + 20);
        });
    }

    // Initial draw
    drawChart();

    // Redraw on window resize
    let resizeTimeout;
    window.addEventListener("resize", function () {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(drawChart, 100);
    });
});
