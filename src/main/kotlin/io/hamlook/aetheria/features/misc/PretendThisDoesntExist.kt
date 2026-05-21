package io.hamlook.aetheria.features.misc

import io.hamlook.aetheria.command.SimpleCommand
import io.hamlook.aetheria.init.RegisterCommand
import net.minecraft.command.ICommandSender

@RegisterCommand
class PretendThisDoesntExist : SimpleCommand() {
    
    override fun getName() = "ATHRthisisatestdontusethispls"
    
    override fun getUsage() = "/ATHRthisisatestdontusethispls"
    
    override fun execute(sender: ICommandSender, args: Array<String>) {
        DVD.forceCornerHit()
    }
}
