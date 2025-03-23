package com.aki.advanced_industry.api.util;

import com.aki.akisutils.apis.util.list.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RaytraceUtil {
    public static Pair<Vec3d, Vec3d> getRayTraceVectors(EntityPlayer player) {
        float pitch = player.rotationPitch;
        float yaw = player.rotationYaw;
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        float f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = -MathHelper.cos(-pitch * 0.017453292F);
        float f4 = MathHelper.sin(-pitch * 0.017453292F);
        float f5 = f2 * f3;
        float f6 = f1 * f3;
        double d3 = 5.0D;
        if (player instanceof EntityPlayerMP) {
            d3 = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        }
        Vec3d end = start.add(new Vec3d(f5 * d3, f4 * d3, f6 * d3));
        return Pair.of(start, end);
    }
}
