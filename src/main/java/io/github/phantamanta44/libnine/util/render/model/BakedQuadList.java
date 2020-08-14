package io.github.phantamanta44.libnine.util.render.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.*;

public class BakedQuadList {

    private final List<BakedQuad> unsidedQuads = new ArrayList<>();
    private final EnumMap<EnumFacing, List<BakedQuad>> sidedQuads = new EnumMap<>(EnumFacing.class);

    public void addQuad(@Nullable EnumFacing face, BakedQuad quad) {
        if (face == null) {
            unsidedQuads.add(quad);
        } else {
            getOrCreateSidedList(face).add(quad);
        }
    }

    public void addQuads(@Nullable EnumFacing face, Iterator<BakedQuad> quads) {
        if (face == null) {
            while (quads.hasNext()) {
                unsidedQuads.add(quads.next());
            }
        } else {
            List<BakedQuad> sidedQuadList = getOrCreateSidedList(face);
            while (quads.hasNext()) {
                sidedQuadList.add(quads.next());
            }
        }
    }

    public void addQuads(@Nullable EnumFacing face, Collection<BakedQuad> quads) {
        if (face == null) {
            unsidedQuads.addAll(quads);
        } else {
            getOrCreateSidedList(face).addAll(quads);
        }
    }

    private List<BakedQuad> getOrCreateSidedList(EnumFacing face) {
        return sidedQuads.computeIfAbsent(face, k -> new ArrayList<>());
    }

    public List<BakedQuad> getQuads(@Nullable EnumFacing face) {
        if (face == null) {
            return unsidedQuads;
        }
        List<BakedQuad> quads = sidedQuads.get(face);
        return quads != null ? quads : Collections.emptyList();
    }

}
