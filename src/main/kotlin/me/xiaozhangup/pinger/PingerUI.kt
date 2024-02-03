package me.xiaozhangup.pinger

import com.pequla.server.ping.ServerPing
import java.awt.*
import java.awt.event.*
import java.awt.geom.RoundRectangle2D
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URL
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*

class PingerUI(
    address: InetAddress,
    port: Int
) : JFrame() {
    private var initialClick: Point? = null
    private var thread: Thread? = null
    private var scale: Double = 1.0

    private val width = 200
    private val height = 100

    private val textArea = JTextArea()
    private val ping = ServerPing(InetSocketAddress(address, port))

    init {
        // 设置窗口的样式为无标题栏
        isUndecorated = true
        isAlwaysOnTop = true
        shape = makeShape(width, height)

        // 设置窗口的大小和位置
        setSize(width, height)
        setLocationRelativeTo(null)

        // 添加鼠标事件监听器
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                initialClick = e.point
            }
        })
        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                // 获取当前鼠标位置
                val currentLocation = e.locationOnScreen

                // 计算窗口的新位置
                val offsetX = currentLocation.x - initialClick!!.x
                val offsetY = currentLocation.y - initialClick!!.y
                setLocation(offsetX, offsetY)
            }
        })

        // 添加鼠标滚轮事件监听器
        addMouseWheelListener { e -> // 获取鼠标滚轮滚动的单位值
            val scrollAmount = e.unitsToScroll

            // 根据滚动方向调整缩放比例
            if (scrollAmount < 0) {
                scale *= 1.1
            } else {
                scale *= 0.9
            }

            // 设置窗口的缩放比例
            val scaledWidth = (width * scale).toInt()
            val scaledHeight = (height * scale).toInt()
            setSize(scaledWidth, scaledHeight)
            shape = makeShape(scaledWidth, scaledHeight)
        }

        // 设置界面元素
        layout = BorderLayout()
        add(refreshButton(this), BorderLayout.SOUTH)

        // 创建图标
        val result = ping.fetchData()
        val icon = ImageIcon(decodeBase64ToImage(result.favicon.substringAfter(",")))
        val iconLabel = JLabel(icon)

        iconImage = icon.image

        // 设置图标的位置和大小
        iconLabel.setBounds(10, 10, icon.iconWidth, icon.iconHeight)
        iconLabel.background = Color.LIGHT_GRAY
        iconLabel.cursor = Cursor(Cursor.MOVE_CURSOR)

        // 创建文字区域
        textArea.text = "获取数据中..."

        // 设置文字区域的位置和大小
        textArea.setBounds(150, 10, width, height)
        textArea.background = Color.WHITE
        textArea.foreground = Color.BLACK

        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        textArea.isEditable = false
        textArea.dragEnabled = false

        textArea.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                // 判断按下的键是否为Esc键
                if (e.keyCode == KeyEvent.VK_ESCAPE) {
                    // 关闭窗口
                    dispose()
                }
            }
        })


        // 添加图标到窗口的西部（左边）
        add(iconLabel, BorderLayout.WEST)

        // 添加文字区域到窗口的中部
        add(textArea, BorderLayout.CENTER)

        // 设置并显示窗口
        opacity = 0.8f
        isVisible = true

        fetchData(ping)
        thread = Thread {
            while (true) {
                Thread.sleep(5000)
                fetchData(ping)
            }
        }
        thread?.start()
    }

    override fun paint(g: Graphics) {
        // 开启抗锯齿
        (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        super.paint(g)
    }

    override fun dispose() {
        thread?.interrupt()
        super.dispose()
    }

    private fun refreshButton(ui: PingerUI): JButton {
        return JButton("点击刷新").apply {
            val buttonHeight = height / 5
            setBounds(0, height - buttonHeight, width, buttonHeight)

            cursor = Cursor(Cursor.HAND_CURSOR)
            background = Color.LIGHT_GRAY
            border = BorderFactory.createLineBorder(Color.LIGHT_GRAY)

            addActionListener { fetchData(ping) }

            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    // 判断按下的键是否为Esc键
                    if (e.keyCode == KeyEvent.VK_ESCAPE) {
                        // 关闭窗口
                        ui.dispose()
                    }
                }
            })
        }
    }

    private fun fetchData(ping: ServerPing) {
        val result = ping.fetchData()
        textArea.text = "${result.players.online}/${result.players.max}\n${result.version.name}\n\n${getCurrentTime()}"
    }
}

fun makeShape(width: Int, height: Int): Shape {
    return RoundRectangle2D.Double(
        0.0,
        0.0,
        width.toDouble(),
        height.toDouble(),
        20.0,
        20.0
    )
}

private fun decodeBase64ToImage(base64Text: String): Image? {
    try {
        val imageBytes = Base64.getDecoder().decode(base64Text)
        val inputStream = ByteArrayInputStream(imageBytes)
        val bufferedImage = ImageIO.read(inputStream)
        return bufferedImage
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

private fun getCurrentTime(): String {
    val currentTime = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    return currentTime.format(formatter)
}