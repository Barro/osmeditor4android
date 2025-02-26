package de.blau.android.easyedit.turnrestriction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.view.Menu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import de.blau.android.R;
import de.blau.android.easyedit.EasyEditManager;
import de.blau.android.easyedit.NonSimpleActionModeCallback;
import de.blau.android.exception.OsmIllegalOperationException;
import de.blau.android.exception.StorageException;
import de.blau.android.osm.Node;
import de.blau.android.osm.OsmElement;
import de.blau.android.osm.Result;
import de.blau.android.osm.Way;
import de.blau.android.util.Util;

public class RestrictionWaySplittingActionModeCallback extends NonSimpleActionModeCallback {
    private static final String DEBUG_TAG = "Restriction...";

    private final Way        way;
    private final Way        fromWay;
    private List<OsmElement> nodes = new ArrayList<>();
    private final int        subTitle;

    /**
     * Construct a WaySplittingActionModeCallback from an existing Way
     * 
     * @param manager the current EasyEditMAnager instance
     * @param subTitle the resource id of the sub title
     * @param way the existing Way
     * @param fromWay the current from segment or null
     * @param results saved intermediate results
     */
    public RestrictionWaySplittingActionModeCallback(@NonNull EasyEditManager manager, int subTitle, @NonNull Way way, @Nullable Way fromWay,
            @Nullable Map<OsmElement, Result> results) {
        super(manager);
        this.way = way;
        this.fromWay = fromWay;
        nodes.addAll(way.getNodes());
        if (!way.isClosed()) {
            // remove first and last node
            nodes.remove(0);
            nodes.remove(nodes.size() - 1);
        }
        this.subTitle = subTitle;
        if (results != null) {
            this.savedResults = results;
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        helpTopic = R.string.help_waysplitting;
        super.onCreateActionMode(mode, menu);
        mode.setSubtitle(subTitle);
        logic.setClickableElements(new HashSet<>(nodes));
        logic.setReturnRelations(false);
        return true;
    }

    @Override
    public boolean handleElementClick(OsmElement element) { // due to clickableElements, only valid nodes can be clicked
        super.handleElementClick(element);
        // protect against race conditions
        if (!(element instanceof Node)) {
            unexpectedElement(DEBUG_TAG, element);
            return false;
        }
        if (way.isClosed()) {
            main.startSupportActionMode(new RestrictionClosedWaySplittingActionModeCallback(manager, way, (Node) element, fromWay, savedResults));
        } else {
            splitSafe(Util.wrapInList(way), () -> {
                try {
                    List<Result> result = logic.performSplit(main, way, (Node) element, true);
                    Way newWay = newWayFromSplitResult(result);
                    if (newWay != null) {
                        saveSplitResult(way, result);
                        if (fromWay == null) {
                            Set<OsmElement> candidates = new HashSet<>();
                            candidates.add(way);
                            candidates.add(newWay);
                            main.startSupportActionMode(new RestartFromElementActionModeCallback(manager, candidates, candidates, savedResults));
                        } else {
                            Way viaWay = fromWay.hasCommonNode(way) ? way : newWay;
                            main.startSupportActionMode(new ViaElementActionModeCallback(manager, fromWay, viaWay, savedResults));
                        }
                    }
                } catch (OsmIllegalOperationException | StorageException ex) {
                    // toast has already been displayed
                    manager.finish();
                }
            });
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mode.setSubtitle("");
        logic.setClickableElements(null);
        logic.setReturnRelations(true);
        super.onDestroyActionMode(mode);
    }
}
