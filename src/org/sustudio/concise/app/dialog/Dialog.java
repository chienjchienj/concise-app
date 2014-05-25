package org.sustudio.concise.app.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.mihalis.opal.opalDialog.ChoiceItem;
import org.mihalis.opal.utils.ResourceManager;
import org.sustudio.concise.app.Concise;

public class Dialog extends org.mihalis.opal.opalDialog.Dialog {

	public Dialog(Shell shell) {
		super(shell);
	}

	/**
	 * Create a dialog box that asks a question
	 * 
	 * @param title title of the dialog box
	 * @param text text of the question
	 * @param defaultValue default value of the input
	 * @return the value typed by the user
	 */
	public static String ask(final String title, final String text, final String defaultValue) {
		return ask(Concise.getActiveApp(), title, text, defaultValue);
	}

	/**
	 * Create a dialog box that asks a question
	 * 
	 * @shell parent shell
	 * @param title title of the dialog box
	 * @param text text of the question
	 * @param defaultValue default value of the input
	 * @return the value typed by the user
	 */
	public static String ask(final Shell shell, final String title, final String text, final String defaultValue) {
		final Dialog dialog = new Dialog(shell);
		dialog.setTitle(ResourceManager.getLabel(ResourceManager.INPUT));
		dialog.getMessageArea().setTitle(title).setText(text).setIcon(Display.getCurrent().getSystemImage(SWT.ICON_INFORMATION)).addTextBox(defaultValue);
		dialog.setButtonType(OpalDialogType.OK_CANCEL);
		if (dialog.show() == 0) {
			return dialog.getMessageArea().getTextBoxValue();
		} else {
			return null;
		}
	}

	/**
	 * Create a dialog box that displays an error message
	 * 
	 * @param title title of the dialog box
	 * @param errorMessage Error message
	 */
	public static void error(final String title, final String errorMessage) {
		error(Concise.getActiveApp(), title, errorMessage);
	}

	/**
	 * Create a dialog box that displays an error message
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param errorMessage Error message
	 */
	public static void error(final Shell shell, final String title, final String errorMessage) {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				final Dialog dialog = new Dialog(shell);
				dialog.setTitle(ResourceManager.getLabel(ResourceManager.APPLICATION_ERROR));
				dialog.getMessageArea().setTitle(title).//
						setText(errorMessage).//
						setIcon(Display.getCurrent().getSystemImage(SWT.ICON_ERROR));
				dialog.setButtonType(OpalDialogType.OK);
				dialog.show();
			}
		});
	}

	/**
	 * Create a dialog box that inform the user
	 * 
	 * @param title title of the dialog box
	 * @param text text to display
	 */
	public static void inform(final String title, final String text) {
		inform(Concise.getActiveApp(), title, text);
	}

	/**
	 * Create a dialog box that inform the user
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param text text to display
	 */
	public static void inform(final Shell shell, final String title, final String text) {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				final Dialog dialog = new Dialog(shell);
				dialog.setTitle(ResourceManager.getLabel(ResourceManager.INFORMATION));
				dialog.getMessageArea().setTitle(title).setText(text).setIcon(Display.getCurrent().getSystemImage(SWT.ICON_INFORMATION));
				dialog.setButtonType(OpalDialogType.CLOSE);
				dialog.show();
			}
		});
		
	}

	/**
	 * Create a dialog box that asks the user a confirmation
	 * 
	 * @param title title of the dialog box
	 * @param text text to display
	 * @return <code>true</code> if the user confirmed, <code>false</code> otherwise
	 */
	public static boolean isConfirmed(final String title, final String text) {
		return isConfirmed(Concise.getActiveApp(), title, text, -1);
	}

	/**
	 * Create a dialog box that asks the user a confirmation
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param text text to display
	 * @return <code>true</code> if the user confirmed, <code>false</code> otherwise
	 */
	public static boolean isConfirmed(final Shell shell, final String title, final String text) {
		return isConfirmed(shell, title, text, -1);
	}

	/**
	 * Create a dialog box that asks the user a confirmation. The button "yes" is not enabled before timer seconds
	 * 
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param timer number of seconds before enabling the yes button
	 * @return <code>true</code> if the user confirmed, <code>false</code> otherwise
	 */
	public static boolean isConfirmed(final String title, final String text, final int timer) {
		return isConfirmed(Concise.getActiveApp(), title, text, timer);
	}

	/**
	 * Create a dialog box that asks the user a confirmation. The button "yes" is not enabled before timer seconds
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param timer number of seconds before enabling the yes button
	 * @return <code>true</code> if the user confirmed, <code>false</code> otherwise
	 */
	public static boolean isConfirmed(final Shell shell, final String title, final String text, final int timer) {
		final Dialog dialog = new Dialog(shell);
		dialog.setTitle(ResourceManager.getLabel(ResourceManager.WARNING));
		dialog.getMessageArea().setTitle(title).setText(text).setIcon(Display.getCurrent().getSystemImage(SWT.ICON_WARNING));

		dialog.getFooterArea().setTimer(timer).setTimerIndexButton(0);
		dialog.setButtonType(OpalDialogType.YES_NO);
		return dialog.show() == 0;
	}

	/**
	 * Create a dialog box with a radio choice
	 * 
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param defaultSelection index of the default selection
	 * @param values values to display
	 * @return the index of the selection
	 */
	public static int radioChoice(final String title, final String text, final int defaultSelection, final String... values) {
		return radioChoice(Concise.getActiveApp(), title, text, defaultSelection, values);
	}

	/**
	 * Create a dialog box with a radio choice
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param defaultSelection index of the default selection
	 * @param values values to display
	 * @return the index of the selection
	 */
	public static int radioChoice(final Shell shell, final String title, final String text, final int defaultSelection, final String... values) {
		final Dialog dialog = new Dialog(shell);
		dialog.setTitle(ResourceManager.getLabel(ResourceManager.CHOICE));
		dialog.getMessageArea().setTitle(title).setText(text).setIcon(Display.getCurrent().getSystemImage(SWT.ICON_QUESTION)).addRadioButtons(defaultSelection, values);
		dialog.setButtonType(OpalDialogType.SELECT_CANCEL);
		if (dialog.show() == 0) {
			return dialog.getMessageArea().getRadioChoice();
		} else {
			return -1;
		}
	}

	/**
	 * Display a dialog box with an exception
	 * 
	 * @param exception exception to display
	 */
	public static void showException(final Throwable exception) {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				final Dialog dialog = new Dialog(Concise.getActiveApp());
				dialog.setTitle(ResourceManager.getLabel(ResourceManager.EXCEPTION));

				final String msg = exception.getMessage();
				final String className = exception.getClass().getName();
				final boolean noMessage = msg == null || msg.trim().length() == 0;

				dialog.getMessageArea().setTitle(noMessage ? className : msg).//
						setText(noMessage ? "" : className).//
						setIcon(Display.getCurrent().getSystemImage(SWT.ICON_ERROR)).//
						setException(exception);

				dialog.getFooterArea().setExpanded(true);

				dialog.setButtonType(OpalDialogType.CLOSE);
				dialog.show();
			}
		});
	}

	/**
	 * Create a dialog box with a choice
	 * 
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param defaultSelection index of the default selection
	 * @param items items to display
	 * @return the index of the selected value
	 */
	public static int choice(final String title, final String text, final int defaultSelection, final ChoiceItem... items) {
		return choice(Concise.getActiveApp(), title, text, defaultSelection, items);
	}

	/**
	 * Create a dialog box with a choice
	 * 
	 * @param shell parent shell
	 * @param title title of the dialog box
	 * @param text text to display
	 * @param defaultSelection index of the default selection
	 * @param items items to display
	 * @return the index of the selected value
	 */
	public static int choice(final Shell shell, final String title, final String text, final int defaultSelection, final ChoiceItem... items) {
		final Dialog dialog = new Dialog(shell);
		dialog.setTitle(ResourceManager.getLabel(ResourceManager.CHOICE));
		dialog.getMessageArea().setTitle(title).setText(text).setIcon(Display.getCurrent().getSystemImage(SWT.ICON_QUESTION)).addChoice(defaultSelection, items);
		dialog.setButtonType(OpalDialogType.NONE);
		dialog.show();
		return dialog.getMessageArea().getChoice();
	}
	
}
