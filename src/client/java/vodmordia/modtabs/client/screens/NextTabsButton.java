package vodmordia.modtabs.client.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabRenderer;

import java.util.List;

/**
 * Button for navigating between pages when there are too many tabs to display
 */
public class NextTabsButton extends ButtonWidget {

    private final List<TabBase> allTabs;
    private final Runnable onPageChange;
    private int currentPage = 0;
    private final int tabsPerPage;

    public NextTabsButton(int x, int y, List<TabBase> allTabs, int tabsPerPage, Runnable onPageChange) {
        super(x, y, TabBase.TAB_WIDTH, TabBase.TAB_HEIGHT, Text.literal("→"),
              button -> ((NextTabsButton) button).nextPage(), DEFAULT_NARRATION_SUPPLIER);
        this.allTabs = allTabs;
        this.tabsPerPage = tabsPerPage;
        this.onPageChange = onPageChange;
    }

    private void nextPage() {
        if (allTabs == null || allTabs.isEmpty()) return;

        int maxPages = (int) Math.ceil((double) allTabs.size() / tabsPerPage);
        currentPage = (currentPage + 1) % maxPages;

        if (onPageChange != null) {
            onPageChange.run();
        }
    }

    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (allTabs == null || allTabs.isEmpty()) return;

        boolean isHovered = isMouseOver(mouseX, mouseY);
        boolean shouldShow = allTabs.size() > tabsPerPage;

        if (!shouldShow) {
            this.visible = false;
            return;
        }

        this.visible = true;

        // Render using TabRenderer for consistency
        TabRenderer.builder()
                .withBackground()
                .withCustomIcon(ctx -> {
                    // Render a simple arrow icon
                    int centerX = ctx.x + TabBase.TAB_WIDTH / 2;
                    int centerY = ctx.y + TabBase.TAB_HEIGHT / 2;

                    // Simple arrow made of filled rectangles
                    ctx.gui.fill(centerX - 2, centerY - 3, centerX, centerY + 3, 0xFF000000);
                    ctx.gui.fill(centerX - 4, centerY - 1, centerX - 2, centerY + 1, 0xFF000000);
                })
                .render(context, getX(), getY(), isHovered, false);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        if (allTabs == null || allTabs.isEmpty()) return;

        int maxPages = (int) Math.ceil((double) allTabs.size() / tabsPerPage);
        this.currentPage = Math.max(0, Math.min(page, maxPages - 1));
    }

    public int getTabsPerPage() {
        return tabsPerPage;
    }

    public List<TabBase> getTabsForCurrentPage() {
        if (allTabs == null || allTabs.isEmpty()) return List.of();

        int startIndex = currentPage * tabsPerPage;
        int endIndex = Math.min(startIndex + tabsPerPage, allTabs.size());

        if (startIndex >= allTabs.size()) return List.of();

        return allTabs.subList(startIndex, endIndex);
    }
}