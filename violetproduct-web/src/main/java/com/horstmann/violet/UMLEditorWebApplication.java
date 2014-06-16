/*
 Violet - A program for editing UML diagrams.

 Copyright (C) 2002 Cay S. Horstmann (http://horstmann.com)

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.horstmann.violet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.horstmann.violet.framework.dialog.DialogFactory;
import com.horstmann.violet.framework.dialog.DialogFactoryMode;
import com.horstmann.violet.framework.file.GraphFile;
import com.horstmann.violet.framework.file.IFile;
import com.horstmann.violet.framework.file.LocalFile;
import com.horstmann.violet.framework.file.chooser.IFileChooserService;
import com.horstmann.violet.framework.file.chooser.JFileChooserService;
import com.horstmann.violet.framework.file.persistence.IFilePersistenceService;
import com.horstmann.violet.framework.file.persistence.XHTMLPersistenceService;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.BeanFactory;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.BeanInjector;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.InjectedBean;
import com.horstmann.violet.framework.plugin.PluginLoader;
import com.horstmann.violet.framework.theme.ClassicMetalTheme;
import com.horstmann.violet.framework.theme.ITheme;
import com.horstmann.violet.framework.theme.ThemeManager;
import com.horstmann.violet.framework.userpreferences.DefaultUserPreferencesDao;
import com.horstmann.violet.framework.userpreferences.IUserPreferencesDao;
import com.horstmann.violet.workspace.IWorkspace;
import com.horstmann.violet.workspace.Workspace;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBootstrapTheme;
import eu.webtoolkit.jwt.WEnvironment;

/**
 * A program for editing UML diagrams.
 */
public class UMLEditorWebApplication extends WApplication {

	private static boolean FACTORY_INITIALIZED = false;

	@InjectedBean
	private PluginLoader pluginLoader;

	/**
	 * Default constructor
	 * 
	 * @param filesToOpen
	 * @throws IOException
	 */
	public UMLEditorWebApplication(WEnvironment env) throws IOException {
		super(env);
		if (!FACTORY_INITIALIZED) {
			initBeanFactory();
			BeanInjector.getInjector().inject(this);
			installPlugins();
			FACTORY_INITIALIZED = true;
		}
		createDefaultWorkspace();
	}

	private void initBeanFactory() {
		IUserPreferencesDao userPreferencesDao = new DefaultUserPreferencesDao();
		BeanFactory.getFactory().register(IUserPreferencesDao.class, userPreferencesDao);

		ThemeManager themeManager = new ThemeManager();
		ITheme theme1 = new ClassicMetalTheme();
		List<ITheme> themeList = new ArrayList<ITheme>();
		themeList.add(theme1);
		themeManager.setInstalledThemes(themeList);
		themeManager.switchToTheme(theme1);
		BeanFactory.getFactory().register(ThemeManager.class, themeManager);

		DialogFactory dialogFactory = new DialogFactory(DialogFactoryMode.DELEGATED);
		BeanFactory.getFactory().register(DialogFactory.class, dialogFactory);

		IFilePersistenceService filePersistenceService = new XHTMLPersistenceService();
		BeanFactory.getFactory().register(IFilePersistenceService.class, filePersistenceService);

		IFileChooserService fileChooserService = new JFileChooserService();
		BeanFactory.getFactory().register(IFileChooserService.class, fileChooserService);
	}

	private void createDefaultWorkspace() throws IOException {
		setTheme(new WBootstrapTheme());

		URL resource = getClass().getResource("test.class.violet.html");
		IFile aFile = new LocalFile(new File(resource.getFile()));
		GraphFile graphFile = new GraphFile(aFile);
		IWorkspace workspace = new Workspace(graphFile);
		workspace.getAWTComponent().setSize(800, 600);
		workspace.getAWTComponent().prepareLayout();
		WorkspaceWidget workspaceWidget = new WorkspaceWidget(workspace);
		getRoot().addWidget(workspaceWidget);
	}

	/**
	 * Install plugins
	 */
	private void installPlugins() {

		this.pluginLoader.installPlugins();
	}

}