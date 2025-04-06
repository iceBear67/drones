package io.ib67.drones.client.renderer;

import io.ib67.drones.entity.DroneEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.AllayEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.AllayEntityRenderState;
import net.minecraft.util.Identifier;

public class DroneAllayRenderer extends MobEntityRenderer<DroneEntity, AllayEntityRenderState, AllayEntityModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/allay/allay.png");

    public DroneAllayRenderer(EntityRendererFactory.Context context) {
        super(context,  new AllayEntityModel(context.getPart(EntityModelLayers.ALLAY)), 0.4f);
    }

    @Override
    public Identifier getTexture(AllayEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public AllayEntityRenderState createRenderState() {
        return new AllayEntityRenderState();
    }
}
