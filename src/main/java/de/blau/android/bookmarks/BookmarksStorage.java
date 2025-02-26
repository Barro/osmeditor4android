package de.blau.android.bookmarks;

import java.io.Serializable;

import androidx.annotation.NonNull;
import de.blau.android.osm.ViewBox;

/**
 * Storage for a comments and viewbox for a bookmark
 */
public class BookmarksStorage implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    static final String NAME_FIELD = "name";

    private ViewBox viewBox;
    private String  comments;

    /**
     * Storage for comment and viewbox
     *
     * @param comments Bookmark name/comment
     * @param viewBox Map viewbox
     */
    public BookmarksStorage(@NonNull String comments, @NonNull ViewBox viewBox) {
        this.comments = comments;
        this.viewBox = viewBox;
    }

    /**
     * Default constructor
     */
    public BookmarksStorage() {
    }

    /**
     * Sets the comments and viewbox for a bookmark object
     *
     * @param comments Bookmark name/comment
     * @param viewBox Map viewbox
     */
    public void set(@NonNull String comments, @NonNull ViewBox viewBox) {
        this.comments = comments;
        this.viewBox = viewBox;
    }

    /**
     * Return the comments for a bookmark object
     *
     * @return an AlertDialog instance
     */
    @NonNull
    public String getComment() {
        return comments;
    }

    /**
     * Get the view box for a bookmark
     * 
     * @return the viewBox
     */
    @NonNull
    public ViewBox getViewBox() {
        return viewBox;
    }
}