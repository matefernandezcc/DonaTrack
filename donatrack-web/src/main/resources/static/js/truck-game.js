/**
 * DonaTrack · Minijuego "Ruta Solidaria" (Estilo Chrome Dino Runner)
 * El camión de DonaTrack esquiva pozos (baches) y lomas de burro en su camino a las ONGs.
 */
(function () {
  "use strict";

  const CANVAS_WIDTH = 880;
  const CANVAS_HEIGHT = 250;
  const GROUND_Y = 204;
  const GRAVITY = 0.68;
  const JUMP_FORCE = -13.2;
  const FAST_FALL_GRAVITY = 1.9;
  const INITIAL_SPEED = 6.2;
  const MAX_SPEED = 13.5;
  const SPEED_INCREMENT = 0.0018;
  const HIGHSCORE_KEY = "dt_truck_dino_highscore";

  function initTruckGame() {
    const canvas = document.getElementById("truck-game-canvas");
    if (!canvas) return;
    const ctx = canvas.getContext("2d");

    const btnJump = document.getElementById("truck-btn-jump");
    const btnDuck = document.getElementById("truck-btn-duck");
    const btnRestart = document.getElementById("truck-btn-restart");
    const scoreEl = document.getElementById("truck-ui-score");
    const hiScoreEl = document.getElementById("truck-ui-hiscore");
    const statusBadge = document.getElementById("truck-ui-status");

    let gameState = "IDLE"; // IDLE | PLAYING | GAME_OVER
    let score = 0;
    let highScore = 0;
    let packagesCollected = 0;
    let speed = INITIAL_SPEED;
    let distanceTraveled = 0;
    let lastTime = 0;
    let crashReason = "";
    let milestoneFlashTimer = 0;
    let milestoneValue = 0;
    let nextSpawnDistance = 320;

    try {
      highScore = parseInt(localStorage.getItem(HIGHSCORE_KEY) || "0", 10) || 0;
    } catch (e) {
      highScore = 0;
    }

    const truck = {
      x: 72,
      y: GROUND_Y - 44,
      width: 76,
      height: 44,
      normalHeight: 44,
      duckHeight: 34,
      vy: 0,
      isJumping: false,
      isDucking: false,
      wheelAngle: 0,
      suspensionOffset: 0
    };

    let obstacles = [];
    let bonuses = [];
    let clouds = [];
    let pebbles = [];
    let exhaustParticles = [];
    let floatingTexts = [];

    function initScenery() {
      clouds = [
        { x: 140, y: 48, w: 54, speedFactor: 0.22 },
        { x: 420, y: 34, w: 68, speedFactor: 0.18 },
        { x: 710, y: 60, w: 48, speedFactor: 0.25 }
      ];
      pebbles = [];
      for (let i = 0; i < 28; i++) {
        pebbles.push({
          x: Math.random() * CANVAS_WIDTH,
          y: GROUND_Y + 6 + Math.random() * 28,
          w: 2 + Math.random() * 6,
          h: 2
        });
      }
    }

    function isDarkMode() {
      return document.documentElement.getAttribute("data-theme") === "dark";
    }

    function formatScore(num) {
      return String(Math.floor(num)).padStart(5, "0");
    }

    function updateDomScore() {
      if (scoreEl) scoreEl.textContent = formatScore(score);
      if (hiScoreEl) hiScoreEl.textContent = formatScore(highScore);
    }

    function resetGame() {
      score = 0;
      packagesCollected = 0;
      speed = INITIAL_SPEED;
      distanceTraveled = 0;
      crashReason = "";
      milestoneFlashTimer = 0;
      nextSpawnDistance = 280;

      truck.height = truck.normalHeight;
      truck.y = GROUND_Y - truck.height;
      truck.vy = 0;
      truck.isJumping = false;
      truck.isDucking = false;

      obstacles = [];
      bonuses = [];
      exhaustParticles = [];
      floatingTexts = [];

      gameState = "PLAYING";
      if (statusBadge) {
        statusBadge.textContent = "EN RUTA";
        statusBadge.className = "pill pill--green";
      }
      updateDomScore();
    }

    function jump() {
      if (gameState === "IDLE" || gameState === "GAME_OVER") {
        resetGame();
        return;
      }
      if (!truck.isJumping) {
        truck.vy = JUMP_FORCE;
        truck.isJumping = true;
        truck.isDucking = false;
        truck.height = truck.normalHeight;
        // Spawn jump dust particles
        for (let i = 0; i < 5; i++) {
          exhaustParticles.push({
            x: truck.x + 15 + Math.random() * 30,
            y: GROUND_Y - 2,
            vx: -1 - Math.random() * 2,
            vy: -0.5 - Math.random() * 1.5,
            r: 2 + Math.random() * 3,
            alpha: 0.75
          });
        }
      }
    }

    function setDuck(active) {
      if (gameState !== "PLAYING") return;
      truck.isDucking = active;
      if (truck.isJumping && active) {
        truck.vy += 2.2;
      } else if (!truck.isJumping) {
        truck.height = active ? truck.duckHeight : truck.normalHeight;
        truck.y = GROUND_Y - truck.height;
      }
    }

    function spawnObstacle() {
      const rand = Math.random();
      if (rand < 0.48) {
        // POZO / BACHE EN EL ASFALTO
        const isWide = Math.random() < 0.35 && speed > 7.5;
        const width = isWide ? 68 : 50;
        obstacles.push({
          type: "POZO",
          label: "POZO",
          x: CANVAS_WIDTH + 20,
          y: GROUND_Y - 8,
          width: width,
          height: 26
        });
      } else {
        // LOMA DE BURRO
        const isDouble = Math.random() < 0.3 && speed > 8;
        const width = isDouble ? 66 : 48;
        const height = isDouble ? 28 : 23;
        obstacles.push({
          type: "LOMA",
          label: "LOMA DE BURRO",
          x: CANVAS_WIDTH + 20,
          y: GROUND_Y - height,
          width: width,
          height: height
        });
      }

      // Chance to spawn floating bonus donation box above or between obstacles
      if (Math.random() < 0.35) {
        bonuses.push({
          x: CANVAS_WIDTH + 160 + Math.random() * 80,
          y: GROUND_Y - 88 - Math.random() * 28,
          width: 26,
          height: 26,
          collected: false
        });
      }
    }

    function checkCollision(a, b) {
      return (
        a.x < b.x + b.width &&
        a.x + a.width > b.x &&
        a.y < b.y + b.height &&
        a.y + a.height > b.y
      );
    }

    function triggerGameOver(reason) {
      gameState = "GAME_OVER";
      crashReason = reason;
      if (score > highScore) {
        highScore = Math.floor(score);
        try {
          localStorage.setItem(HIGHSCORE_KEY, String(highScore));
        } catch (e) {}
      }
      if (statusBadge) {
        statusBadge.textContent = "DETENIDO";
        statusBadge.className = "pill pill--orange";
      }
      updateDomScore();
    }

    function update(dtFactor) {
      if (gameState !== "PLAYING") return;

      speed = Math.min(MAX_SPEED, speed + SPEED_INCREMENT * dtFactor);
      const moveX = speed * dtFactor;
      distanceTraveled += moveX;

      const prevScoreHundreds = Math.floor(score / 100);
      score += 0.15 * dtFactor * (speed / INITIAL_SPEED);
      const currScoreHundreds = Math.floor(score / 100);

      if (currScoreHundreds > prevScoreHundreds && currScoreHundreds > 0) {
        milestoneFlashTimer = 36;
        milestoneValue = currScoreHundreds * 100;
      }
      if (milestoneFlashTimer > 0) {
        milestoneFlashTimer -= dtFactor;
      }

      if (score > highScore) {
        highScore = Math.floor(score);
      }
      updateDomScore();

      // Truck physics
      truck.wheelAngle += moveX * 0.14;
      truck.suspensionOffset = truck.isJumping ? 0 : Math.sin(distanceTraveled * 0.18) * 1.3;

      if (truck.isJumping) {
        const g = truck.isDucking ? FAST_FALL_GRAVITY : GRAVITY;
        truck.vy += g * dtFactor;
        truck.y += truck.vy * dtFactor;

        const targetHeight = truck.isDucking ? truck.duckHeight : truck.normalHeight;
        if (truck.y >= GROUND_Y - targetHeight) {
          truck.height = targetHeight;
          truck.y = GROUND_Y - truck.height;
          truck.vy = 0;
          truck.isJumping = false;
        }
      }

      // Exhaust particles
      if (Math.random() < 0.35 * dtFactor) {
        exhaustParticles.push({
          x: truck.x - 4,
          y: truck.y + truck.height - 10,
          vx: -1.8 - Math.random() * 1.2,
          vy: -0.4 - Math.random() * 0.6,
          r: 2.5 + Math.random() * 3,
          alpha: 0.55
        });
      }

      for (let i = exhaustParticles.length - 1; i >= 0; i--) {
        const p = exhaustParticles[i];
        p.x += p.vx * dtFactor;
        p.y += p.vy * dtFactor;
        p.r += 0.08 * dtFactor;
        p.alpha -= 0.025 * dtFactor;
        if (p.alpha <= 0) exhaustParticles.splice(i, 1);
      }

      // Clouds & Pebbles
      clouds.forEach(function (c) {
        c.x -= moveX * c.speedFactor;
        if (c.x + c.w < -20) {
          c.x = CANVAS_WIDTH + 30 + Math.random() * 60;
          c.y = 26 + Math.random() * 48;
        }
      });

      pebbles.forEach(function (p) {
        p.x -= moveX;
        if (p.x + p.w < 0) {
          p.x = CANVAS_WIDTH + Math.random() * 40;
          p.y = GROUND_Y + 6 + Math.random() * 28;
        }
      });

      // Spawn obstacles
      nextSpawnDistance -= moveX;
      if (nextSpawnDistance <= 0) {
        spawnObstacle();
        const minGap = 230 + speed * 14;
        const maxGap = 410 + speed * 18;
        nextSpawnDistance = minGap + Math.random() * (maxGap - minGap);
      }

      // Forgiving truck hitbox
      const truckHitbox = {
        x: truck.x + 10,
        y: truck.y + 6,
        width: truck.width - 18,
        height: truck.height - 8
      };

      // Update obstacles
      for (let i = obstacles.length - 1; i >= 0; i--) {
        const obs = obstacles[i];
        obs.x -= moveX;

        const obsHitbox =
          obs.type === "POZO"
            ? {
                x: obs.x + 10,
                y: GROUND_Y - 10,
                width: obs.width - 20,
                height: 16
              }
            : {
                x: obs.x + 6,
                y: obs.y + 4,
                width: obs.width - 12,
                height: obs.height - 4
              };

        if (checkCollision(truckHitbox, obsHitbox)) {
          if (obs.type === "POZO") {
            triggerGameOver("¡Caíste en un pozo profundo! Saltá antes del cono vial.");
          } else {
            triggerGameOver("¡Impactaste una loma de burro a alta velocidad!");
          }
          return;
        }

        if (obs.x + obs.width < -40) {
          obstacles.splice(i, 1);
        }
      }

      // Update bonus packages
      for (let i = bonuses.length - 1; i >= 0; i--) {
        const b = bonuses[i];
        b.x -= moveX;

        if (!b.collected && checkCollision(truckHitbox, b)) {
          b.collected = true;
          score += 50;
          packagesCollected++;
          floatingTexts.push({
            text: "+50 📦",
            x: b.x,
            y: b.y,
            alpha: 1.0
          });
        }

        if (b.collected || b.x + b.width < -20) {
          bonuses.splice(i, 1);
        }
      }

      // Floating texts
      for (let i = floatingTexts.length - 1; i >= 0; i--) {
        const ft = floatingTexts[i];
        ft.y -= 1.1 * dtFactor;
        ft.alpha -= 0.025 * dtFactor;
        if (ft.alpha <= 0) floatingTexts.splice(i, 1);
      }
    }

    function drawCloud(c, dark) {
      ctx.save();
      ctx.fillStyle = dark ? "rgba(148, 163, 184, 0.16)" : "rgba(100, 116, 139, 0.18)";
      ctx.beginPath();
      ctx.roundRect(c.x, c.y, c.w, 14, 7);
      ctx.roundRect(c.x + 10, c.y - 8, c.w * 0.55, 14, 7);
      ctx.fill();
      ctx.restore();
    }

    function drawRoad(dark) {
      // Asphalt road base
      ctx.fillStyle = dark ? "#111827" : "#e2e8f0";
      ctx.fillRect(0, GROUND_Y, CANVAS_WIDTH, CANVAS_HEIGHT - GROUND_Y);

      // Main Chrome Dino horizon line
      ctx.strokeStyle = dark ? "#64748b" : "#334155";
      ctx.lineWidth = 2.5;
      ctx.beginPath();
      ctx.moveTo(0, GROUND_Y);
      ctx.lineTo(CANVAS_WIDTH, GROUND_Y);
      ctx.stroke();

      // Pebbles / texture streaks
      ctx.fillStyle = dark ? "#334155" : "#94a3b8";
      pebbles.forEach(function (p) {
        ctx.fillRect(p.x, p.y, p.w, p.h);
      });
    }

    function drawObstacle(obs, dark) {
      ctx.save();
      if (obs.type === "POZO") {
        // Cut out the road line to look like a real gap/pothole in the asphalt
        ctx.fillStyle = dark ? "#030712" : "#0f172a";
        ctx.beginPath();
        ctx.moveTo(obs.x, GROUND_Y - 2);
        ctx.lineTo(obs.x + 8, GROUND_Y + 20);
        ctx.lineTo(obs.x + obs.width - 8, GROUND_Y + 20);
        ctx.lineTo(obs.x + obs.width, GROUND_Y - 2);
        ctx.closePath();
        ctx.fill();

        // Jagged asphalt rim
        ctx.strokeStyle = "#ef4444";
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(obs.x - 2, GROUND_Y);
        ctx.lineTo(obs.x + 8, GROUND_Y + 18);
        ctx.lineTo(obs.x + obs.width * 0.5, GROUND_Y + 13);
        ctx.lineTo(obs.x + obs.width - 8, GROUND_Y + 18);
        ctx.lineTo(obs.x + obs.width + 2, GROUND_Y);
        ctx.stroke();

        // Small warning traffic cone before the pothole
        const coneX = obs.x - 10;
        const coneY = GROUND_Y;
        ctx.fillStyle = "#f97316";
        ctx.beginPath();
        ctx.moveTo(coneX, coneY);
        ctx.lineTo(coneX + 6, coneY - 16);
        ctx.lineTo(coneX + 12, coneY);
        ctx.closePath();
        ctx.fill();

        // White reflective stripe on cone
        ctx.fillStyle = "#ffffff";
        ctx.fillRect(coneX + 3, coneY - 9, 6, 3);

        // Label
        ctx.fillStyle = dark ? "#fca5a5" : "#dc2626";
        ctx.font = "bold 9px monospace";
        ctx.textAlign = "center";
        ctx.fillText("POZO", obs.x + obs.width / 2, GROUND_Y + 32);
      } else {
        // LOMA DE BURRO (Speed bump with yellow/black diagonal hazard stripes)
        const centerX = obs.x + obs.width / 2;
        ctx.beginPath();
        ctx.moveTo(obs.x, GROUND_Y);
        ctx.quadraticCurveTo(centerX, GROUND_Y - obs.height * 1.75, obs.x + obs.width, GROUND_Y);
        ctx.closePath();
        ctx.fillStyle = "#eab308";
        ctx.fill();
        ctx.lineWidth = 2;
        ctx.strokeStyle = dark ? "#fef08a" : "#1e293b";
        ctx.stroke();

        // Hazard stripes inside the speed bump
        ctx.save();
        ctx.clip();
        ctx.fillStyle = "#1e293b";
        for (let sx = obs.x - 10; sx < obs.x + obs.width + 10; sx += 12) {
          ctx.beginPath();
          ctx.moveTo(sx, GROUND_Y + 2);
          ctx.lineTo(sx + 6, GROUND_Y + 2);
          ctx.lineTo(sx + 14, GROUND_Y - obs.height - 6);
          ctx.lineTo(sx + 8, GROUND_Y - obs.height - 6);
          ctx.closePath();
          ctx.fill();
        }
        ctx.restore();

        // Label
        ctx.fillStyle = dark ? "#fde047" : "#a16207";
        ctx.font = "bold 9px monospace";
        ctx.textAlign = "center";
        ctx.fillText("LOMA", centerX, GROUND_Y + 15);
      }
      ctx.restore();
    }

    function drawBonus(b) {
      ctx.save();
      // Cardboard donation box with DonaTrack ribbon
      ctx.fillStyle = "#d97706";
      ctx.beginPath();
      ctx.roundRect(b.x, b.y, b.width, b.height, 4);
      ctx.fill();
      ctx.strokeStyle = "#fef3c7";
      ctx.lineWidth = 1.5;
      ctx.stroke();

      // Cross tape
      ctx.fillStyle = "#fde68a";
      ctx.fillRect(b.x + b.width / 2 - 2, b.y, 4, b.height);
      ctx.fillRect(b.x, b.y + b.height / 2 - 2, b.width, 4);
      ctx.restore();
    }

    function drawTruck(dark) {
      ctx.save();
      const tx = truck.x;
      const ty = truck.y + truck.suspensionOffset;
      const tw = truck.width;
      const th = truck.height;

      // Slight pitch angle when jumping
      if (truck.isJumping) {
        const angle = Math.max(-0.18, Math.min(0.18, truck.vy * 0.014));
        ctx.translate(tx + tw / 2, ty + th / 2);
        ctx.rotate(angle);
        ctx.translate(-(tx + tw / 2), -(ty + th / 2));
      }

      // Headlight beam in dark mode
      if (dark && gameState === "PLAYING") {
        const grad = ctx.createLinearGradient(tx + tw, ty + th - 12, tx + tw + 95, ty + th);
        grad.addColorStop(0, "rgba(253, 224, 71, 0.28)");
        grad.addColorStop(1, "rgba(253, 224, 71, 0)");
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.moveTo(tx + tw, ty + th - 15);
        ctx.lineTo(tx + tw + 95, ty + th - 32);
        ctx.lineTo(tx + tw + 95, ty + th + 4);
        ctx.closePath();
        ctx.fill();
      }

      // Chassis base
      ctx.fillStyle = dark ? "#334155" : "#1e293b";
      ctx.fillRect(tx + 4, ty + th - 12, tw - 6, 5);

      // Cargo box (furgón solidario)
      const cargoW = 48;
      const cargoH = th - 10;
      ctx.fillStyle = dark ? "#f8fafc" : "#ffffff";
      ctx.strokeStyle = dark ? "#38bdf8" : "#0b4a8f";
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.roundRect(tx, ty, cargoW, cargoH, 4);
      ctx.fill();
      ctx.stroke();

      // DonaTrack green stripe on cargo box
      ctx.fillStyle = "#16a34a";
      ctx.fillRect(tx + 2, ty + cargoH - 10, cargoW - 4, 5);

      // DonaTrack label on cargo box
      ctx.fillStyle = "#0b4a8f";
      ctx.font = "900 10px 'Inter', sans-serif";
      ctx.textAlign = "center";
      ctx.fillText("DONA", tx + cargoW / 2, ty + cargoH / 2);

      // Truck Cab (Cabina)
      const cabX = tx + cargoW + 2;
      const cabW = tw - cargoW - 2;
      const cabH = th - 16;
      const cabY = ty + 6;
      ctx.fillStyle = "#0b4a8f";
      ctx.beginPath();
      ctx.roundRect(cabX, cabY, cabW, cabH, [2, 8, 3, 2]);
      ctx.fill();

      // Windshield
      ctx.fillStyle = dark ? "#7dd3fc" : "#bae6fd";
      ctx.beginPath();
      ctx.moveTo(cabX + 5, cabY + 4);
      ctx.lineTo(cabX + cabW - 5, cabY + 4);
      ctx.lineTo(cabX + cabW - 2, cabY + 14);
      ctx.lineTo(cabX + 5, cabY + 14);
      ctx.closePath();
      ctx.fill();

      // Headlight
      ctx.fillStyle = "#fde047";
      ctx.fillRect(cabX + cabW - 3, cabY + cabH - 8, 3, 5);

      // Wheels (front & rear)
      const wheelY = ty + th - 6;
      const wheelPositions = [tx + 16, tx + tw - 14];
      wheelPositions.forEach(function (wx) {
        ctx.save();
        ctx.translate(wx, wheelY);
        ctx.rotate(truck.wheelAngle);
        // Tire
        ctx.fillStyle = "#0f172a";
        ctx.beginPath();
        ctx.arc(0, 0, 8, 0, Math.PI * 2);
        ctx.fill();
        // Rim
        ctx.fillStyle = "#cbd5e1";
        ctx.beginPath();
        ctx.arc(0, 0, 3.8, 0, Math.PI * 2);
        ctx.fill();
        // Spoke line
        ctx.strokeStyle = "#475569";
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.moveTo(-4, 0);
        ctx.lineTo(4, 0);
        ctx.stroke();
        ctx.restore();
      });

      ctx.restore();
    }

    function drawHUD(dark) {
      ctx.save();
      ctx.font = "bold 15px 'Courier New', monospace";
      ctx.textAlign = "right";
      ctx.fillStyle = dark ? "#94a3b8" : "#475569";

      const hiText = "HI " + formatScore(highScore);
      const scoreText =
        milestoneFlashTimer > 0 && Math.floor(milestoneFlashTimer / 4) % 2 === 0
          ? formatScore(milestoneValue)
          : formatScore(score);

      ctx.fillText(hiText + "   " + scoreText, CANVAS_WIDTH - 22, 30);

      // Speed & bonus indicator on top-left
      ctx.textAlign = "left";
      ctx.font = "bold 12px 'Courier New', monospace";
      const speedMult = (speed / INITIAL_SPEED).toFixed(1);
      ctx.fillText(
        "VEL " + speedMult + "x   📦 ENTREGAS: " + packagesCollected,
        20,
        30
      );
      ctx.restore();
    }

    function drawOverlays(dark) {
      if (gameState === "IDLE") {
        ctx.save();
        ctx.fillStyle = dark ? "rgba(15, 23, 42, 0.72)" : "rgba(248, 250, 252, 0.78)";
        ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        ctx.fillStyle = dark ? "#f8fafc" : "#0f172a";
        ctx.font = "800 18px 'Manrope', 'Inter', sans-serif";
        ctx.textAlign = "center";
        ctx.fillText(
          "🚚 RUTA SOLIDARIA · ESQUIVÁ POZOS Y LOMAS DE BURRO",
          CANVAS_WIDTH / 2,
          CANVAS_HEIGHT / 2 - 14
        );

        ctx.fillStyle = dark ? "#38bdf8" : "#0b4a8f";
        ctx.font = "700 14px 'Courier New', monospace";
        ctx.fillText(
          "PRESIONÁ [ESPACIO], [↑] O TOCÁ LA PANTALLA PARA ARRANCAR",
          CANVAS_WIDTH / 2,
          CANVAS_HEIGHT / 2 + 16
        );
        ctx.restore();
      } else if (gameState === "GAME_OVER") {
        ctx.save();
        ctx.fillStyle = dark ? "rgba(15, 23, 42, 0.78)" : "rgba(248, 250, 252, 0.82)";
        ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Chrome Dino style spaced "G A M E   O V E R"
        ctx.fillStyle = dark ? "#f8fafc" : "#1e293b";
        ctx.font = "bold 22px 'Courier New', monospace";
        ctx.textAlign = "center";
        ctx.fillText("G A M E   O V E R", CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 28);

        ctx.fillStyle = dark ? "#fca5a5" : "#dc2626";
        ctx.font = "600 13px 'Inter', sans-serif";
        ctx.fillText(crashReason, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 2);

        // Classic Dino replay button box
        const btnX = CANVAS_WIDTH / 2 - 20;
        const btnY = CANVAS_HEIGHT / 2 + 12;
        ctx.fillStyle = "#16a34a";
        ctx.beginPath();
        ctx.roundRect(btnX, btnY, 40, 34, 6);
        ctx.fill();

        // Replay circular arrow icon
        ctx.strokeStyle = "#ffffff";
        ctx.lineWidth = 2.5;
        ctx.beginPath();
        ctx.arc(CANVAS_WIDTH / 2, btnY + 17, 9, 0.4, Math.PI * 1.85);
        ctx.stroke();

        ctx.fillStyle = dark ? "#cbd5e1" : "#475569";
        ctx.font = "bold 12px 'Courier New', monospace";
        ctx.fillText(
          "Presioná [ESPACIO] o hacé clic para reintentar",
          CANVAS_WIDTH / 2,
          btnY + 62
        );
        ctx.restore();
      }
    }

    function render() {
      const dark = isDarkMode();

      // Sky background
      ctx.fillStyle = dark ? "#0b1120" : "#f8fafc";
      ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

      // Clouds
      clouds.forEach(function (c) {
        drawCloud(c, dark);
      });

      // Road & Horizon
      drawRoad(dark);

      // Obstacles (Pozos & Lomas de burro)
      obstacles.forEach(function (obs) {
        drawObstacle(obs, dark);
      });

      // Bonus packages
      bonuses.forEach(function (b) {
        if (!b.collected) drawBonus(b);
      });

      // Exhaust particles
      exhaustParticles.forEach(function (p) {
        ctx.save();
        ctx.fillStyle = dark
          ? "rgba(148, 163, 184, " + Math.max(0, p.alpha) + ")"
          : "rgba(100, 116, 139, " + Math.max(0, p.alpha) + ")";
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
      });

      // Truck
      drawTruck(dark);

      // Floating score popups
      floatingTexts.forEach(function (ft) {
        ctx.save();
        ctx.fillStyle = "rgba(22, 163, 74, " + Math.max(0, ft.alpha) + ")";
        ctx.font = "bold 13px 'Courier New', monospace";
        ctx.fillText(ft.text, ft.x, ft.y);
        ctx.restore();
      });

      // HUD Score
      drawHUD(dark);

      // State Overlays
      drawOverlays(dark);
    }

    function loop(timestamp) {
      if (!lastTime) lastTime = timestamp;
      const elapsed = timestamp - lastTime;
      lastTime = timestamp;

      // Normalize to 60 FPS (16.67ms per frame), clamped to avoid huge jumps when tab is inactive
      const dtFactor = Math.min(2.5, Math.max(0.2, elapsed / 16.667));

      update(dtFactor);
      render();
      requestAnimationFrame(loop);
    }

    // Solo capturar teclado si la partida está activa o el usuario enfocó el lienzo del minijuego
    let gameFocused = false;
    const gameSection = document.getElementById("minijuego");

    canvas.addEventListener("focus", function () {
      gameFocused = true;
    });
    canvas.addEventListener("blur", function () {
      gameFocused = false;
    });
    document.addEventListener("pointerdown", function (e) {
      gameFocused = !!(gameSection && gameSection.contains(e.target));
    });

    function isGameInViewport() {
      const rect = canvas.getBoundingClientRect();
      return rect.top < window.innerHeight && rect.bottom > 0;
    }

    window.addEventListener("keydown", function (e) {
      const active = document.activeElement;
      const tag = active ? active.tagName : "";
      if (tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT" || tag === "BUTTON" || tag === "A") return;
      if (!isGameInViewport()) return;
      // No interceptar Espacio/flechas para hacer scroll mientras el usuario solo navega la landing
      const canCapture = gameState === "PLAYING" || gameFocused || active === canvas;
      if (!canCapture) return;

      if (e.code === "Space" || e.code === "ArrowUp" || e.code === "KeyW") {
        e.preventDefault();
        jump();
      } else if (e.code === "ArrowDown" || e.code === "KeyS") {
        if (gameState === "PLAYING") {
          e.preventDefault();
          setDuck(true);
        }
      }
    });

    window.addEventListener("keyup", function (e) {
      if (e.code === "ArrowDown" || e.code === "KeyS") {
        setDuck(false);
      }
    });

    canvas.addEventListener("pointerdown", function (e) {
      e.preventDefault();
      gameFocused = true;
      if (typeof canvas.focus === "function") canvas.focus({ preventScroll: true });
      jump();
    });

    if (btnJump) {
      btnJump.addEventListener("click", function (e) {
        e.preventDefault();
        jump();
      });
    }

    if (btnDuck) {
      btnDuck.addEventListener("pointerdown", function (e) {
        e.preventDefault();
        setDuck(true);
      });
      btnDuck.addEventListener("pointerup", function () {
        setDuck(false);
      });
      btnDuck.addEventListener("pointerleave", function () {
        setDuck(false);
      });
    }

    if (btnRestart) {
      btnRestart.addEventListener("click", function (e) {
        e.preventDefault();
        resetGame();
      });
    }

    initScenery();
    updateDomScore();
    requestAnimationFrame(loop);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initTruckGame);
  } else {
    initTruckGame();
  }
})();
