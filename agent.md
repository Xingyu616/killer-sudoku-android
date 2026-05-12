# Killer Sudoku 安卓单机版 - 项目 Agent 参考规范

## 项目概述
一个独立的 Android 应用，用户可以生成、解决杀手数独谜题。应用支持多个难度级别、进度保存、提示功能等特性。

## 技术栈
- **语言**：Kotlin
- **UI 框架**：Jetpack Compose + Material Design 3
- **架构**：MVVM + Repository Pattern
- **数据存储**：Room ORM + SQLite
- **异步处理**：Kotlin Coroutines
- **依赖注入**：Hilt
- **构建工具**：Gradle

## 项目目录结构
```
app/
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/killersudoku/
│   │   │   ├── ui/                          # UI 层（Composable）
│   │   │   │   ├── screen/                  # 屏幕级组件
│   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   ├── GameScreen.kt
│   │   │   │   │   ├── DifficultyScreen.kt
│   │   │   │   │   └── ...
│   │   │   │   ├── component/               # 可复用组件
│   │   │   │   │   ├── GameGrid.kt
│   │   │   │   │   ├── NumberKeypad.kt
│   │   │   │   │   ├── CageBox.kt
│   │   │   │   │   └── ...
│   │   │   │   ├── theme/                   # Material Design 3 主题
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Typography.kt
│   │   │   │   │   └── Theme.kt
│   │   │   │   └── KillerSudokuApp.kt      # 应用程序入口
│   │   │   ├── viewmodel/                   # ViewModel 层
│   │   │   │   ├── HomeViewModel.kt
│   │   │   │   ├── GameViewModel.kt
│   │   │   │   └── ...
│   │   │   ├── domain/                      # 业务逻辑层
│   │   │   │   ├── model/                   # 数据模型
│   │   │   │   │   ├── Game.kt
│   │   │   │   │   ├── Puzzle.kt
│   │   │   │   │   ├── Cage.kt
│   │   │   │   │   └── ...
│   │   │   │   ├── usecase/                 # 用例层
│   │   │   │   │   ├── GeneratePuzzleUseCase.kt
│   │   │   │   │   ├── ValidateMoveUseCase.kt
│   │   │   │   │   ├── GetHintUseCase.kt
│   │   │   │   │   └── ...
│   │   │   │   └── repository/              # 数据仓储接口
│   │   │   │       ├── GameRepository.kt
│   │   │   │       ├── PuzzleRepository.kt
│   │   │   │       └── ...
│   │   │   ├── data/                        # 数据层
│   │   │   │   ├── local/                   # 本地数据库
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── GameDao.kt
│   │   │   │   │   │   ├── PuzzleDao.kt
│   │   │   │   │   │   └── ...
│   │   │   │   │   └── entity/
│   │   │   │   │       ├── GameEntity.kt
│   │   │   │   │       ├── PuzzleEntity.kt
│   │   │   │   │       └── ...
│   │   │   │   ├── repository/              # Repository 实现
│   │   │   │   │   ├── GameRepositoryImpl.kt
│   │   │   │   │   └── ...
│   │   │   │   └── di/                      # Hilt 依赖注入
│   │   │   │       └── DataModule.kt
│   │   │   ├── util/                        # 工具类
│   │   │   │   ├── Logger.kt
│   │   │   │   ├── Extensions.kt
│   │   │   │   └── ...
│   │   │   └── MainActivity.kt
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml              # 字符串资源（国际化）
│   │   │   │   └── dimens.xml               # 尺寸资源
│   │   │   └── mipmap/                      # 应用图标资源
│   │   └── AndroidManifest.xml
│   └── test/
│       ├── kotlin/                          # 单元测试
│       └── ...
├── build.gradle.kts                         # Gradle 构建配置
└── proguard-rules.pro                       # 混淆规则
```

## Kotlin 编码规范

### 基本规则
- 使用函数式编程风格，优先使用函数而非类
- 遵循 Kotlin 官方 Coding Conventions
- 变量优先使用 `val`（不可变），必要时才用 `var`
- 类型推断优先，不需要显式标注的地方省略类型
- 使用 extension functions 增强可读性和代码复用

### Composable 函数规范
```kotlin
// ✅ 推荐
@Composable
fun GameGrid(
    puzzle: Puzzle,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 实现
    }
}

// ❌ 不推荐
@Composable
fun GameGridOld(puzzle: Puzzle, onCellClick: (Int, Int) -> Unit) {
    // ...
}
```

**规则**：
- 所有参数都应有默认值（特别是 `Modifier`）
- 可组合函数应保持高内聚、低耦合
- 避免在 Composable 内部直接进行业务逻辑计算，使用 ViewModel
- 使用 `remember` 缓存不需要频繁重组的对象

### ViewModel 状态管理
```kotlin
@HiltViewModel
class GameViewModel @Inject constructor(
    private val validateMoveUseCase: ValidateMoveUseCase,
    private val getHintUseCase: GetHintUseCase,
    private val repository: GameRepository
) : ViewModel() {
    
    private val _gameState = MutableStateFlow<GameState>(GameState.Loading)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    fun onCellClick(row: Int, col: Int) {
        viewModelScope.launch {
            // 处理用户交互
        }
    }
}
```

**规则**：
- 使用 `StateFlow` 或 `LiveData` 暴露不可变的状态
- 在 `viewModelScope` 内启动协程
- ViewModel 不应持有 UI 引用
- 使用 Hilt 进行依赖注入，标注 `@HiltViewModel`

### 协程使用规范
```kotlin
viewModelScope.launch {
    try {
        val result = withContext(Dispatchers.Default) {
            // CPU 密集操作（如谜题生成、求解）
        }
        _uiState.value = result
    } catch (e: Exception) {
        _errorState.value = e.message
    }
}
```

**规则**：
- IO 操作使用 `Dispatchers.IO`
- CPU 密集操作使用 `Dispatchers.Default`
- UI 更新总是在 `Dispatchers.Main`（默认）
- 总是使用 try-catch 处理异常

## 杀手数独核心逻辑规范

### 数据模型定义
```kotlin
// 单个单元格的候选数和值
data class Cell(
    val row: Int,
    val col: Int,
    var value: Int = 0,                  // 0 表示未填
    var candidates: Set<Int> = (1..9).toSet(),  // 可能的数字
    val isGiven: Boolean = false          // 是否为初始给定
)

// Cage（杀手数独的笼）
data class Cage(
    val id: Int,
    val cells: List<Cell>,
    val targetSum: Int,
    val operation: Operation = Operation.ADDITION  // 运算符
)

enum class Operation {
    ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION
}

// 完整的谜题
data class Puzzle(
    val id: String,
    val difficulty: Difficulty,
    val grid: Array<IntArray>,           // 9x9 网格
    val cages: List<Cage>,
    val createdAt: Long
)

enum class Difficulty {
    EASY, MEDIUM, HARD
}
```

### 核心操作规范

#### 1. 谜题验证
```kotlin
// 验证当前移动是否合法
fun validateMove(
    puzzle: Puzzle,
    row: Int,
    col: Int,
    value: Int
): ValidationResult {
    // 检查规则：
    // 1. 行中是否已存在该数字
    // 2. 列中是否已存在该数字
    // 3. 3x3 区域中是否已存在该数字（标准数独）
    // 4. Cage sum 约束是否仍可能满足
    return ValidationResult(isValid, reason)
}
```

#### 2. 提示生成
```kotlin
fun generateHint(puzzle: Puzzle, row: Int, col: Int): Hint {
    // 返回候选数集合，帮助用户推理
    // 使用逻辑推理（naked singles, hidden singles 等）
    // 如果逻辑推理无法确定，返回候选数；否则直接返回答案
    return Hint(candidates = listOf(3, 5, 7))
}
```

#### 3. 自动求解
```kotlin
fun solvePuzzle(puzzle: Puzzle): Puzzle? {
    // 使用回溯算法求解
    // 返回解后的谜题，若无解返回 null
    return solvedPuzzle
}
```

#### 4. 谜题生成（版本 1：预生成题库）
```kotlin
// 预先生成或存储题库
// 后续版本可改为动态生成
fun generatePuzzleForDifficulty(difficulty: Difficulty): Puzzle {
    // 根据难度返回相应的谜题
}
```

### 难度参数配置
```kotlin
object DifficultyConfig {
    val EASY = DifficultyLevel(
        emptyCount = 30..40,     // 空白数量范围
        cageCount = 15..18,
        maxCageSize = 5
    )
    val MEDIUM = DifficultyLevel(
        emptyCount = 45..55,
        cageCount = 18..22,
        maxCageSize = 4
    )
    val HARD = DifficultyLevel(
        emptyCount = 60..70,
        cageCount = 22..25,
        maxCageSize = 3
    )
}
```

## Jetpack Compose UI 规范

### 可组合函数规范
- 所有参数都应有默认值（至少 `modifier: Modifier = Modifier`）
- 状态应由调用者通过参数传入（状态提升原则）
- 事件回调作为最后一个参数

```kotlin
@Composable
fun GameGrid(
    puzzle: Puzzle,
    gameState: GameState,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 实现
}
```

### 主题与样式
- 统一使用 Material Design 3 颜色、排版、形状
- 在 `theme/Theme.kt` 中定义应用全局主题
- 避免硬编码颜色值，使用 `MaterialTheme.colorScheme` 和 `MaterialTheme.typography`

```kotlin
@Composable
fun KillerSudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography,
        shapes = appShapes,
        content = content
    )
}
```

### 网格布局实现
- 使用 `LazyGrid` 或 `Box` 配合 `Column/Row` 构建 9x9 网格
- Cage 通过背景边框和不同颜色区分
- 单元格状态（已选中、有错误等）通过背景色表示

```kotlin
@Composable
fun SudokuCell(
    value: String,
    isSelected: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(cellSize)
            .background(
                when {
                    isError -> Color.Red.copy(alpha = 0.2f)
                    isSelected -> Color.Blue.copy(alpha = 0.1f)
                    else -> Color.White
                }
            )
            .border(1.dp, Color.Gray)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
```

### 用户交互
- **点击填数字**：点击网格单元格后弹出或显示数字键盘
- **删除**：长按单元格或点击删除按钮
- **撤销/重做**：通过 ViewModel 维护操作栈
- **提示**：点击提示按钮调用 ViewModel 方法

## 数据持久化规范

### Room 实体定义
```kotlin
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val puzzleId: String,
    val difficulty: String,
    val currentGrid: String,              // JSON 序列化的网格
    val startTime: Long,
    val lastModified: Long,
    val isCompleted: Boolean = false,
    val completedTime: Long? = null
)

@Entity(
    tableName = "puzzles",
    foreignKeys = [ForeignKey(entity = GameEntity::class, parentColumns = ["id"], childColumns = ["gameId"])]
)
data class PuzzleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameId: Int,
    val initialGrid: String,              // JSON 序列化
    val cagesJson: String,                // JSON 序列化的 cage 信息
    val difficulty: String
)
```

### DAO 规范
```kotlin
@Dao
interface GameDao {
    @Insert
    suspend fun insertGame(game: GameEntity): Long
    
    @Update
    suspend fun updateGame(game: GameEntity)
    
    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: Int): GameEntity?
    
    @Query("SELECT * FROM games ORDER BY lastModified DESC")
    fun getAllGames(): Flow<List<GameEntity>>
}
```

### 序列化与反序列化
- 复杂对象（如网格、cage 列表）使用 JSON 序列化存储
- 使用 Kotlinx Serialization 或 Gson，保持一致性
- 提供类型转换函数便于 Entity 与 Domain Model 之间的转换

```kotlin
// 转换函数示例
fun GameEntity.toDomainModel(): Game = Game(
    id = id.toString(),
    puzzleId = puzzleId,
    currentGrid = currentGrid.deserializeGrid(),
    startTime = startTime,
    isCompleted = isCompleted
)
```

## 应用全局规范与最佳实践

### 依赖注入（Hilt）
```kotlin
@HiltAndroidApp
class KillerSudokuApp : Application()

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "killer_sudoku.db").build()
    }
    
    @Provides
    fun provideGameDao(db: AppDatabase): GameDao = db.gameDao()
}
```

### 异常处理
- 定义自定义异常层次结构
- 在 Repository 或 ViewModel 层捕获异常并转换为 UI 友好的错误信息

```kotlin
sealed class GameError : Exception() {
    data class InvalidMove(override val message: String) : GameError()
    data class DatabaseError(override val message: String) : GameError()
    object NetworkError : GameError()
}
```

### 日志记录
- 使用统一的日志工具
- DEBUG 版本打印详细日志，RELEASE 版本只记录错误

```kotlin
object Logger {
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) Log.d(tag, msg)
    }
    fun e(tag: String, msg: String, e: Exception? = null) {
        Log.e(tag, msg, e)
    }
}
```

### 测试策略
- **单元测试**：业务逻辑层（谜题验证、提示生成、求解器）
- **集成测试**：Repository 和 DAO
- **UI 测试**：关键交互流程（使用 Compose Testing API）

```kotlin
class GameViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Test
    fun testValidMove() {
        // 测试有效的移动
    }
}
```

### 性能优化
- 谜题生成和求解应在后台线程执行，避免阻塞 UI
- 使用 `remember` 和 `derivedStateOf` 避免不必要的重组
- 定期清理过期的已完成游戏数据
- 避免在网格绘制时进行复杂计算

### 资源管理
- 所有字符串使用 `strings.xml` 资源文件，便于国际化
- 颜色、尺寸等常量定义在对应的 resource 文件中
- 应用图标和其他资源应满足 Material Design 规范

### 代码风格工具
- 使用 Ktlint 进行代码格式检查
- 使用 Detekt 进行代码质量分析
- 集成到 CI/CD 流程

---

**最后更新**：2026年5月
**项目目标**：单机版 Android 杀手数独应用
