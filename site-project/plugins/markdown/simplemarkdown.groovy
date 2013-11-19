
pluginMarkDownConverter('simple markdown' , new MarkDownConverter().&toHTML);


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

