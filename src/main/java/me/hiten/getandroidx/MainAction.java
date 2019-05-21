package me.hiten.getandroidx;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import me.hiten.getandroidx.fragment.MainFragment;
import org.jetbrains.annotations.NotNull;

public class MainAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new MainFragment().show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Project project = e.getProject();
        Presentation presentation = e.getPresentation();
        if (project == null) {
            presentation.setEnabled(false);
            return;
        }
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            presentation.setEnabled(false);
            return;
        }
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (file == null) {
            presentation.setEnabled(false);
            return;
        }
        String name = file.getName().replaceAll("\"", "");
        if (!name.endsWith(".gradle")) {
            presentation.setEnabled(false);
        }

    }
}
