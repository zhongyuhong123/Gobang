/* 游戏模式选择 */
.game-modes {
  margin-bottom: 40px;
}

.section-title {
  text-align: center;
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 30px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.modes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 25px;
  max-width: 800px;
  margin: 0 auto;
}

.mode-card {
  background: rgba(30, 41, 59, 0.9);
  border-radius: var(--radius-lg);
  padding: 30px 20px;
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 2px solid transparent;
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-lg);
}

.mode-card:hover {
  transform: translateY(-5px);
  border-color: var(--primary-color);
  box-shadow: var(--shadow-xl);
}

.mode-card.gobang:hover {
  border-color: #000;
  background: rgba(0, 0, 0, 0.2);
}

.mode-card.military:hover {
  border-color: #4CAF50;
  background: rgba(76, 175, 80, 0.1);
}

.mode-card.chinese-chess:hover {
  border-color: #FF9800;
  background: rgba(255, 152, 0, 0.1);
}

.mode-icon {
  font-size: 3rem;
  margin-bottom: 15px;
  display: block;
}

.mode-card h3 {
  font-size: 1.3rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.mode-card p {
  color: var(--text-secondary);
  font-size: 14px;
}

/* 游戏操作区域 */
.game-actions {
  display: flex;
  gap: 30px;
  max-width: 1000px;
  width: 100%;
  justify-content: center;
  flex-wrap: wrap;
  animation: fadeIn 0.6s ease-out;
}

.placeholder-text {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.chess-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.6;
  animation: float 3s ease-in-out infinite;
}