package me.xiaozhangup.pinger
import java.net.InetAddress

fun main() {
    val ip = "r2.dimc.cloud:22003".split(':')
    PingerUI(
        InetAddress.getByName(ip[0]),
        ip[1].toInt()
    )
}