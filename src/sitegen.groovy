/*
 * Copyright 2013 samuraiproducts.com
 */
import groovy.text.SimpleTemplateEngine
import groovy.transform.ToString

import java.nio.file.Path

// configuration
PROJECT_PATH = './site'
GENERATION_PATH = PROJECT_PATH + '/html'
CSS_PATH = PROJECT_PATH + '/assets/css'
JS_PATH = PROJECT_PATH + '/assets/js'
IMAGE_PATH = PROJECT_PATH + '/assets/img'
PAGES_PATH = PROJECT_PATH + '/pages'
TEMPLATES_PATH = PROJECT_PATH + '/templates'
PAGE_FILE_EXTENSION = '.page'
HTML_FILE_EXTENSION = '.html'

// dirs
def genDir = new File(GENERATION_PATH)
def pagesDir = new File(PAGES_PATH)
def templatesDir = new File(TEMPLATES_PATH)
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
info 'Generatrion start...'

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
        contents = page.&contents
        // evaluate .page file
        evaluate(file)
        page.date = new Date(file.lastModified())
        def logic = new MarkDownConverter();
        page.converter = logic.&toHTML
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
    enginebinding['page'] = it

    template = engine.createTemplate(file.text).make(enginebinding)

    String pagePath = it.file.getPath()
    pagePath = pagePath.replaceFirst('^' + PAGES_PATH, GENERATION_PATH)
    pagePath = pagePath.replaceFirst(PAGE_FILE_EXTENSION + '$' , HTML_FILE_EXTENSION)

    File toFile = new File(pagePath)
    Path patn = toFile.toPath().parent

    // relative dirs
    enginebinding['css']=patn.relativize(cssPath)
    enginebinding['js']=patn.relativize(jsPath)
    enginebinding['img']=patn.relativize(imagePath)

    toFile.getParentFile().mkdirs()
    toFile.write(template.toString())
    println toFile
}


info 'Generatrion end'
// finish !



// classes
class Site{
    def List<Page> pages =[]
    def Map<String , File> templateFiles = [:]
    def addPage(Page p){
        pages.add(p)
    }
    def info(Object message){
        println '[SITE GEN INFO] ' + message.toString()
    }
}

@ToString
class Page{
    Site site
    String pageName
    String pageTitle
    String pageContents
    String pageTemplate
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

class MarkDownConverter{
    StringBuffer html

    def String toHTML(String markdown){
        html = new StringBuffer()
        markdown.eachLine { line ->
            String sharps = line.find(/^#+ /);
            if(sharps != null){
                def sharpSize = sharps.trim().size()
                def contents = line.replaceAll(/^#+\s*|\s*#+$/, '').trim()
                addHtml ("<h${sharpSize}>" + contents +"</h${sharpSize}>")
            }else{
                addHtml ("<div>" + line +"</div>")
            }
            if(line.startsWith(/^#+/)){ addHeader(line)}
        }
        return html.toString()
    }

    def addHtml(String text){
        html.append(text).append('\n');
    }

}
