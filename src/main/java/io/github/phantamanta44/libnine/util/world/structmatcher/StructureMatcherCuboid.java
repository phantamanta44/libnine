package io.github.phantamanta44.libnine.util.world.structmatcher;

import io.github.phantamanta44.libnine.util.math.MathUtils;
import io.github.phantamanta44.libnine.util.tuple.IPair;
import io.github.phantamanta44.libnine.util.world.WorldBlockPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StructureMatcherCuboid implements IStructureMatcher {

    private final CorePosition corePos;
    private final IStructureMatcher wallMatcher, bodyMatcher;
    private int volumeMin = -1, volumeMax = -1;
    @Nullable
    private IStructureMatcher floorMatcher, ceilMatcher, wallEdgeMatcher, floorEdgeMatcher, ceilEdgeMatcher;
    @Nullable
    private CuboidMatcher postTest;

    public StructureMatcherCuboid(CorePosition corePos, IStructureMatcher wallMatcher, IStructureMatcher bodyMatcher) {
        this.corePos = corePos;
        this.wallMatcher = wallMatcher;
        this.bodyMatcher = bodyMatcher;
    }

    public void setMinVolume(int volume) {
        this.volumeMin = volume;
    }

    public void setMaxVolume(int volume) {
        this.volumeMax = volume;
    }

    public void setFloorMatcher(IStructureMatcher matcher) {
        this.floorMatcher = matcher;
    }

    public void setCeilMatcher(IStructureMatcher matcher) {
        this.ceilMatcher = matcher;
    }

    public void setWallEdgeMatcher(IStructureMatcher matcher) {
        this.wallEdgeMatcher = matcher;
    }

    public void setFloorEdgeMatcher(IStructureMatcher matcher) {
        this.floorEdgeMatcher = matcher;
    }

    public void setCeilEdgeMatcher(IStructureMatcher matcher) {
        this.ceilEdgeMatcher = matcher;
    }

    public void setPostTest(CuboidMatcher matcher) {
        this.postTest = matcher;
    }

    @Override
    public boolean testStructure(WorldBlockPos basePos, List<Vec3i> components) {
        IPair<Vec3i, Vec3i> minMax = MathUtils.computeCuboid(components);
        Vec3i min = minMax.getA(), max = minMax.getB();
        int minX = min.getX(), minY = min.getY(), minZ = min.getZ();
        int maxX = max.getX(), maxY = max.getY(), maxZ = max.getZ();
        int dx = maxX - minX + 1, dy = maxY - minY + 1, dz = maxZ - minZ + 1;
        int volume = (dx - 2) * (dy - 2) * (dz - 2);
        if ((volumeMin > 0 && volume < volumeMin) || (volumeMax > 0 && volume > volumeMax)
                || !corePos.isCorePositionValid(min, max)) {
            return false;
        }
        List<Vec3i> floor = new ArrayList<>(), walls = new ArrayList<>(), ceil = new ArrayList<>(), body = new ArrayList<>();
        for (Vec3i pos : components) {
            int x = pos.getX(), y = pos.getY(), z = pos.getZ();
            if (y == minY) {
                floor.add(pos);
            } else if (y == maxY) {
                ceil.add(pos);
            } else if (x == minX || x == maxX || z == minZ || z == maxZ) {
                walls.add(pos);
            } else {
                body.add(pos);
            }
        }
        int surfaceXZ = dx * dz;
        if (floor.size() != surfaceXZ || ceil.size() != surfaceXZ || walls.size() != (dy - 2) * (dx * 2 + dz * 2 - 4)) {
            return false;
        }
        if (floorMatcher != null) {
            if (testFailsXZ(basePos, floor, minX, minZ, maxX, maxZ, floorMatcher, floorEdgeMatcher)) {
                return false;
            }
        } else {
            walls.addAll(floor);
        }
        if (ceilMatcher != null) {
            if (testFailsXZ(basePos, ceil, minX, minZ, maxX, maxZ, ceilMatcher, ceilEdgeMatcher)) {
                return false;
            }
        } else {
            walls.addAll(ceil);
        }
        if (wallEdgeMatcher != null) {
            Iterator<Vec3i> iter = walls.iterator();
            List<Vec3i> wallEdge = new ArrayList<>();
            while (iter.hasNext()) {
                Vec3i pos = iter.next();
                if (pos.getX() == minX || pos.getX() == maxX) {
                    if (pos.getY() == minY || pos.getY() == maxY || pos.getZ() == minZ || pos.getZ() == maxZ) {
                        wallEdge.add(pos);
                        iter.remove();
                    }
                } else if ((pos.getY() == minY || pos.getY() == maxY) && (pos.getZ() == minZ || pos.getZ() == maxZ)) {
                    wallEdge.add(pos);
                    iter.remove();
                }
            }
            if (!wallEdgeMatcher.testStructure(basePos, wallEdge)) {
                return true;
            }
        }
        if (!wallMatcher.testStructure(basePos, walls) || !bodyMatcher.testStructure(basePos, body)) {
            return false;
        }
        return postTest == null || postTest.testCuboid(basePos.getWorld(), basePos.getPos().add(min), basePos.getPos().add(max));
    }

    private static boolean testFailsXZ(WorldBlockPos basePos, List<Vec3i> plane, int minX, int minZ, int maxX, int maxZ,
                                       IStructureMatcher matcher, @Nullable IStructureMatcher edgeMatcher) {
        if (edgeMatcher != null) {
            Iterator<Vec3i> iter = plane.iterator();
            List<Vec3i> edge = new ArrayList<>();
            while (iter.hasNext()) {
                Vec3i pos = iter.next();
                if (pos.getX() == minX || pos.getX() == maxX || pos.getZ() == minZ || pos.getZ() == maxZ) {
                    edge.add(pos);
                    iter.remove();
                }
            }
            if (!edgeMatcher.testStructure(basePos, edge)) {
                return true;
            }
        }
        return !matcher.testStructure(basePos, plane);
    }

    @FunctionalInterface
    public interface CuboidMatcher {

        boolean testCuboid(World world, BlockPos min, BlockPos max);

    }

    public enum CorePosition {

        ANYWHERE {
            @Override
            public boolean isCorePositionValid(Vec3i min, Vec3i max) {
                return min.getX() == 0 || min.getY() == 0 || min.getZ() == 0
                        || max.getX() == 0 || max.getY() == 0 || max.getZ() == 0;
            }
        },
        IN_FACE {
            @Override
            public boolean isCorePositionValid(Vec3i min, Vec3i max) {
                if (min.getX() == 0 || max.getX() == 0) {
                    return !(min.getY() == 0 || max.getY() == 0 || min.getZ() == 0 || max.getZ() == 0);
                } else if (min.getY() == 0 || max.getY() == 0) {
                    return !(min.getZ() == 0 || max.getZ() == 0);
                } else {
                    return min.getZ() == 0 || max.getZ() == 0;
                }
            }
        },
        IN_WALL_OR_EDGE {
            @Override
            public boolean isCorePositionValid(Vec3i min, Vec3i max) {
                return (min.getX() == 0 || max.getX() == 0 || min.getZ() == 0 || max.getZ() == 0)
                        && !(min.getY() == 0 || max.getY() == 0);
            }
        },
        IN_WALL {
            @Override
            public boolean isCorePositionValid(Vec3i min, Vec3i max) {
                if (min.getY() == 0 || max.getY() == 0) {
                    return false;
                } else if (min.getX() == 0 || min.getY() == 0) {
                    return !(min.getZ() == 0 || max.getZ() == 0);
                } else {
                    return min.getZ() == 0 || max.getZ() == 0;
                }
            }
        },
        IN_FLOOR_OR_EDGE {
            @Override
            public boolean isCorePositionValid(Vec3i min, Vec3i max) {
                return min.getY() == 0;
            }
        },
        IN_FLOOR {
            @Override
            public boolean isCorePositionValid(Vec3i min, Vec3i max) {
                return min.getY() == 0 && !(min.getX() == 0 || max.getX() == 0 || min.getZ() == 0 || max.getZ() == 0);
            }
        },
        IN_CEIL_OR_EDGE {
            @Override
            public boolean isCorePositionValid(Vec3i min, Vec3i max) {
                return max.getY() == 0;
            }
        },
        IN_CEIL {
            @Override
            public boolean isCorePositionValid(Vec3i min, Vec3i max) {
                return max.getY() == 0 && !(min.getX() == 0 || max.getX() == 0 || min.getZ() == 0 || max.getZ() == 0);
            }
        };

        public abstract boolean isCorePositionValid(Vec3i min, Vec3i max);

    }

}
