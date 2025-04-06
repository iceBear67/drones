package io.ib67.drones.client.renderer;

import io.ib67.drones.entity.DroneEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BeeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BeeEntityRenderState;
import net.minecraft.util.Identifier;

public class DroneEntityRenderer extends MobEntityRenderer<DroneEntity, BeeEntityRenderState, BeeEntityModel> {
    private static final Identifier BEE_TEXTURE = Identifier.ofVanilla("textures/entity/bee/bee.png");

    public DroneEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new BeeEntityModel(context.getPart(EntityModelLayers.BEE)), 0.4F);
    }

    @Override
    public BeeEntityRenderState createRenderState() {
        return new BeeEntityRenderState();
    }

    @Override
    public Identifier getTexture(BeeEntityRenderState state) {
        return BEE_TEXTURE;
    }
}
