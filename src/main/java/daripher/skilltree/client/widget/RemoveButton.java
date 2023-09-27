package daripher.skilltree.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;

import daripher.skilltree.client.screen.ScreenHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RemoveButton extends ConfirmationButton {
    public RemoveButton(int x, int y, int width, int height, OnPress pressFunc) {
        super(x, y, width, height, Component.empty(), pressFunc);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.renderButton(poseStack, mouseX, mouseY, partialTick);
        ScreenHelper.prepareTextureRendering(new ResourceLocation("skilltree:textures/screen/widgets/remove_icon.png"));
        blit(poseStack, x, y, width, height, 0, confirming ? 14 : 0, 14, 14, 14, 28);
    }
}