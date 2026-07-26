/**
 * 📈 LibraAI Report Analytics Canvas Renderer
 */
document.addEventListener("DOMContentLoaded", function () {
    const canvas = document.getElementById("analyticsCanvas");
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    
    // Sample Data
    const data = [
        { label: "Computer Sci", value: 45, color: "#F97316" },
        { label: "Data Science", value: 30, color: "#3B82F6" },
        { label: "Physics", value: 20, color: "#22C55E" },
        { label: "Math", value: 25, color: "#7C3AED" },
        { label: "Literature", value: 15, color: "#F59E0B" }
    ];

    const startX = 60;
    const startY = 200;
    const chartHeight = 150;
    const barWidth = 50;
    const gap = 30;
    const maxValue = 50;

    // Draw Axis
    ctx.beginPath();
    ctx.strokeStyle = "#E5E7EB";
    ctx.lineWidth = 2;
    ctx.moveTo(startX - 10, startY);
    ctx.lineTo(startX + (data.length * (barWidth + gap)), startY);
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
        ctx.font = "bold 12px Segoe UI";
        ctx.textAlign = "center";
        ctx.fillText(item.value, x + barWidth / 2, y - 8);

        // Draw Label
        ctx.fillStyle = "#6B7280";
        ctx.font = "12px Segoe UI";
        ctx.fillText(item.label, x + barWidth / 2, startY + 20);
    });
});
