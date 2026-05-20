package dev.xylonity.bonsai.ghosts.client.entity.render;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.GeoBone;

record BoneRenderVisibility(GeoBone bone, BoneSnapshot snapshot, boolean hadSnapshot, boolean hidden, boolean childrenHidden) {

    static BoneRenderVisibility setBranchHidden(GeoBone bone, boolean hidden) {
        BoneSnapshot snapshot = bone.frameSnapshot;
        boolean hadSnapshot = snapshot != null;

        if (snapshot == null) {
            snapshot = BoneSnapshot.create(bone);
            bone.frameSnapshot = snapshot;
        }

        BoneRenderVisibility visibility = new BoneRenderVisibility(bone, snapshot, hadSnapshot, snapshot.isHidden(), snapshot.areChildrenHidden());
        snapshot.skipRender(hidden);
        snapshot.skipChildrenRender(hidden);

        return visibility;
    }

    void restore() {
        if (hadSnapshot) {
            snapshot.skipRender(hidden);
            snapshot.skipChildrenRender(childrenHidden);
        } else {
            bone.frameSnapshot = null;
        }
    }

}
