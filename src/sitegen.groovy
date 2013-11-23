/*
 * Copyright 2013 samuraiproducts.com
 */
import groovy.text.SimpleTemplateEngine
import groovy.transform.ToString

import java.nio.file.Path

// configuration
GENERATION_PATH = './'
CSS_PATH = GENERATION_PATH + '/assets/css'
JS_PATH = GENERATION_PATH + '/assets/js'
IMAGE_PATH = GENERATION_PATH + '/assets/img'

CHARSET = 'UTF-8'

PROJECT_PATH = './site-project'
PAGES_PATH = PROJECT_PATH + '/pages'
TEMPLATES_PATH = PROJECT_PATH + '/templates'
PLUGINS_PATH = PROJECT_PATH + '/plugins'
SITE_FILE_EXTENSION = '.site'
PAGE_FILE_EXTENSION = '.page'
PLUGIN_FILE_EXTENSION = '.groovy'
HTML_FILE_EXTENSION = '.html'

// dirs
def projectDir = new File(PROJECT_PATH)
def genDir = new File(GENERATION_PATH)
def pagesDir = new File(PAGES_PATH)
def templatesDir = new File(TEMPLATES_PATH)
def pluginsDir = new File(PLUGINS_PATH)
def cssDir = new File(CSS_PATH)
def jsDir = new File(JS_PATH)
def imageDir = new File(IMAGE_PATH)

// path
def cssPath = cssDir.toPath()
def jsPath = jsDir.toPath()
def imagePath = imageDir.toPath()

// start generation !
def site = new Site();
info = site.&info
info 'GEN DIR      :' + genDir
info 'PAGES DIR    :' + pagesDir
info 'TEMPLATE DIR :' + templatesDir

Plugins plugins = new Plugins();

// plugins file
pluginsDir.eachFileRecurse { file ->
    if(file.name.endsWith(PLUGIN_FILE_EXTENSION)){
        pluginMarkDownConverter = plugins.&pluginMarkDownConverter
        // evaluate plugin file
        evaluate(file)
    }
}

// site file
projectDir.eachFile { file ->
    if(file.name.endsWith(SITE_FILE_EXTENSION)){
        name = site.&name
        // evaluate .site file
        evaluate(file)
        site.file = file
    }
}

// template files
templatesDir.eachFileRecurse {
    site.templateFiles[it.getName()] = it
}


// create pages
info('read pages...')
pagesDir.eachFileRecurse{  file ->
    if(file.name.endsWith(PAGE_FILE_EXTENSION)){

        println file

        // create Page Object
        Page page = new Page(file:file , site:site)
        name = page.&name
        title = page.&title
        template = page.&template
        markdown = page.&markdown
        contents = page.&contents
        // evaluate .page file
        evaluate(file.getText(CHARSET))
        page.date = new Date(file.lastModified())
        Closure cl = plugins.markDownConverters[page.markdown]
        if(cl == null){
            cl = plugins.defaultMarkDownConverter
        }
        page.converter = cl
        site.addPage(page)
    }
}

// create html
info('write html...')
enginebinding = [:]
engine = new SimpleTemplateEngine()
site.pages.each {
    File file = it.templateFile
    if(file==null){return};
    enginebinding['site'] = site
    enginebinding['page'] = it

    template = engine.createTemplate(file.text).make(enginebinding)

    String pagePath = it.file.getPath()
	
	
	println '---'
	println it.file
	
	
	Path pdirPath = pagesDir.toPath()
	Path genPath = genDir.toPath()
	Path pPath = it.file.toPath()
	
	println 'a ' + pdirPath
	println 'b ' + genPath
	println 'c ' + pPath
	Path relativePages = pdirPath.relativize(pPath)
	Path pageGenPath = new File(genPath.toString() + '/' + relativePages.toString()).toPath()
	
	println 'd ' + relativePages
	println 'e ' + pageGenPath

	
	pagePath = pageGenPath.toString();
    pagePath = pagePath.replaceFirst(PAGE_FILE_EXTENSION + '$' , HTML_FILE_EXTENSION)

    File toFile = new File(pagePath)
    Path patn = toFile.toPath().parent

    // relative dirs
    enginebinding['css']=patn.relativize(cssPath)
    enginebinding['js']=patn.relativize(jsPath)
    enginebinding['img']=patn.relativize(imagePath)

    toFile.getParentFile().mkdirs()
    toFile.write(template.toString() , CHARSET)
    println toFile
}


info 'Generatrion end'
// finish !



// classes
class Site{
    def siteName
    File file
    def List<Page> pages =[]
    def Map<String , File> templateFiles = [:]
    def addPage(Page p){
        pages.add(p)
    }
    def info(Object message){
        println '[SITE GEN INFO] ' + message.toString()
    }
    def name(String text){
        this.siteName = text
    }
    String getName(){
        this.siteName
    }
}

@ToString
class Page{
    Site site
    String pageName
    String pageTitle
    String pageContents
    String pageTemplate
    String pageMarkDown
    File file
    Date date
    def converter

    def name(String text){
        this.pageName = text
    }
    String getName(){
        this.pageName
    }
    def title(String text){
        this.pageTitle = text
    }
    String getTitle(){
        this.pageTitle
    }
    def template(String text){
        this.pageTemplate = text
    }
    def markdown(String text){
        this.pageMarkDown = text
    }
    String getMarkdown(){
        this.pageMarkDown
    }
    File getTemplateFile(){
        if(pageTemplate == null){return null}
        File f = site.templateFiles[pageTemplate]
        if(f!=null){return f}
        return null;
    }
    String getTemplate(){
        this.pageTemplate
    }
    def contents(String text){
        this.pageContents = text
    }
    String getContents(){
        this.pageContents
    }
    String getHtml(){
        converter this.pageContents
    }

    def contents(Closure cl){
        cl.call()
    }
}


class Plugins{
    def markDownConverters = [:]
    def defaultMarkDownConverter
    def pluginMarkDownConverter(String id , Closure cl){
        markDownConverters[id] = cl
        if(defaultMarkDownConverter == null){
            defaultMarkDownConverter = cl
        }
    }

}