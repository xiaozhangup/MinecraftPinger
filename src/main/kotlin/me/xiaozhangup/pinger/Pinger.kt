package me.xiaozhangup.pinger
import java.net.InetAddress

fun main() {
    val ip = "s1.dimc.cloud:50001".split(':')
    PingerUI(
        InetAddress.getByName(ip[0]),
        ip[1].toInt()
    )
}