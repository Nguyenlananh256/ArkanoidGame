# Arkanoid Game - Object-Oriented Programming Project

## Author
Group 8 - Class INT2204_1 (OOP 1)  
1.	Nguyễn Thị Lan Anh - 24020019  
2.	Nguyễn Minh Hùng - 24020145  
3.	Đào Văn Toàn - 24020325  
4.	Nguyễn Minh Tú - 24020343

**Instructor:** Kiều Văn Tuyên  
**Semester:** HK1 – 2025–2026 
________________________________________
## Description
This is a classic Arkanoid game developed in Java as a final project for Object-Oriented Programming course. The project demonstrates the implementation of OOP principles and design patterns.  

**Key features:**
1.	The game is developed using Java 17+ with JavaFX/Swing for GUI.  
2.	Implements core OOP principles: Encapsulation, Inheritance, Polymorphism, and Abstraction.  
3.	Applies multiple design patterns: Singleton.  
4.	Includes sound effects, animations, and power-up systems.  
5.	Supports save/load game functionality and leaderboard system.

**Game mechanics:**  

•	Control a paddle to bounce a ball and destroy bricks  
•	Collect power-ups for special abilities  
•	Progress through multiple levels with increasing difficulty  
•	Score points and compete on the leaderboard  
________________________________________
## UML Diagram
### Class Diagram
________________________________________
## Design Patterns Implementation
### 1. Singleton Pattern
**Used in:** HighScoreManager.  

**Purpose:** Ensure only one instance exists throughout the application.  
________________________________________
## Multithreading Implementation
________________________________________
## Installation
1.	Clone the project from the repository.  
2.	Open the project in the IDE.  
3.	Run the project.

## Usage  
### Controls  
  
| Key | Action |
|------|------------|
| ← |	Move paddle left |
| → |	Move paddle right |
| SPACE |	Launch ball / Pause |
| ENTER |	Start game / Next level |
  
### How to Play  
1.	**Start the game:** Press ENTER to start game.  
2.	**Control the paddle:** Use arrow keys to move left and right.  
3.	**Launch the ball:** Press SPACE to launch the ball from the paddle.  
4.	**Destroy bricks:** Bounce the ball to hit and destroy bricks.  
5.	**Collect power-ups:** Catch falling power-ups for special abilities.  
6.	**Avoid losing the ball:** Keep the ball from falling below the paddle.  
7.	**Complete the level:** Destroy all destructible bricks to advance.  
### Power-ups  
  
| Icon |	Name |	Effect |
|------|------------|------|
| <img width="25" height="25" alt="ExpandPaddle" src="https://github.com/user-attachments/assets/a33cb8bb-fdd2-46ec-b70d-aabb33ac209e" /> | Expand Paddle | Increases paddle width for 10 seconds |
| <img width="25" height="25" alt="FastBall" src="https://github.com/user-attachments/assets/5a3b6e56-fb2c-461f-bd1e-5e9fe390a0fd" /> | Fast Ball |	Increases ball speed for 10 seconds |
| <img width="25" height="25" alt="ExtraBall" src="https://github.com/user-attachments/assets/82e17820-f101-45b8-b68e-5398824c47e2" /> | Extra Ball |	Spawns additional ball |
| <img width="25" height="25" alt="ExtraLife" src="https://github.com/user-attachments/assets/44e0c2ce-1cb0-449c-8210-8bd7339f2d2d" /> | Extra Life |	Grants the player one additional life |
| <img width="25" height="25" alt="StrongBall" src="https://github.com/user-attachments/assets/4f4a6f92-08df-4588-b224-5b3c2ed28d15" /> | Strong Ball |	Ball passes through bricks for 2,5 seconds |
  
### Scoring System  
•	Row 1: 60 points  
•	Row 2: 50 points  
•	Row 3: 40 points  
•	Row 2: 20 points  
•	Row 1: 10 points  
•	Strong Brick: points / 3 hits  
•	SilverBrick: 0 points  
•	Explosive Brick:  points + nearby bricks  
•	Combo Multiplier: x2, x3, x4... for consecutive hits  
________________________________________
## Demo
### Screenshots
#### Main Menu

#### Gameplay

#### Power-ups in Action

#### Video Demo

________________________________________
## Future Improvements
#### Planned Features  
1.	**Additional game modes**  
o	Time attack mode  
o	Survival mode with endless levels  
o	Co-op multiplayer mode  
2.	**Enhanced gameplay**  
o	Boss battles at end of worlds  
o	More power-up varieties (freeze time, shield wall, etc.)  
o	Achievements system  
3.	**Technical improvements**  
o	Migrate to LibGDX or JavaFX for better graphics  
o	Add particle effects and advanced animations  
o	Implement AI opponent mode  
o	Add online leaderboard with database backend  
________________________________________
## Technologies Used
| **Technology** | **Version** | **Purpose** |
|-----|-----|----|
| Java | 17+ | Core language |
| JavaFX | 19.0.2 |	GUI framework |
| Maven |	3.9+ | Build tool |
| Jackson | 2.15.0 | JSON processing |
________________________________________
## License  
This project is developed for educational purposes only.  

**Academic Integrity:** This code is provided as a reference. Please follow your institution's academic integrity policies.
________________________________________
## Notes  
•	The game was developed as part of the Object-Oriented Programming with Java course curriculum.  
•	All code is written by group members with guidance from the instructor.  
•	Some assets (images, sounds) may be used for educational purposes under fair use.  
•	The project demonstrates practical application of OOP concepts and design patterns.  
________________________________________
*Last updated: 10/11/2025*
