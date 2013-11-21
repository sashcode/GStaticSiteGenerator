
pluginMarkDownConverter('processing' , new ProcessingConverter().&toHTML);


class ProcessingConverter{
    def String toHTML(String processing){
"""<script type=\"application/processing\">
${processing}
</script>
<canvas width="200" height="200"></canvas>"""
    }
}

