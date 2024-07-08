package me.xiaozhangup.pinger

import com.pequla.server.ping.ServerPing
import java.awt.*
import java.awt.event.*
import java.awt.geom.RoundRectangle2D
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
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

    private val title = JLabel("MinecraftPinger", SwingConstants.CENTER)
    private val label = JLabel("获取数据中...", SwingConstants.CENTER)
    private val version = JLabel("", SwingConstants.CENTER)
    private val time = JLabel("", SwingConstants.CENTER)
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
//            val scrollAmount = e.unitsToScroll
            // 根据滚动方向调整缩放比例
//            if (scrollAmount < 0) {
//                if (scale <= 1.2) scale *= 1.1
//            } else {
//                if (scale >= 1.1) scale *= 0.9
//            }
//
//            // 设置窗口的缩放比例
//            val scaledWidth = (width * scale).toInt()
//            val scaledHeight = (height * scale).toInt()
//            setSize(scaledWidth, scaledHeight)
//            shape = makeShape(scaledWidth, scaledHeight)
        }

        // 设置界面元素
        layout = BorderLayout()
        add(title, BorderLayout.NORTH)
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
        iconLabel.border = BorderFactory.createLineBorder(Color.LIGHT_GRAY)

        label.addKeyListener(object : KeyAdapter() {
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
        val box = Box.createVerticalBox()
        box.border = BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        box.add(label)
        box.add(version)
        box.add(time)
        add(box, BorderLayout.CENTER)

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

//    override fun paint(g: Graphics) {
//        super.paint(g)
//        val g2d = g as Graphics2D
//
//        // 设置抗锯齿
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
//
//        // 绘制阴影
//        val shadowSize = 10
//        val shadowOffset = 10
//        g2d.color = Color(0, 0, 0, 128)
//        g2d.fill(
//            RoundRectangle2D.Float(
//                shadowOffset.toFloat(),
//                shadowOffset.toFloat(),
//                (getWidth() - shadowSize).toFloat(),
//                (getHeight() - shadowSize).toFloat(),
//                20f,
//                20f
//            )
//        )
//    }

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
        label.text = "${result.players.online}/${result.players.max}"
        version.text = result.version.name
        time.text = getCurrentTime()
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