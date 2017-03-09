<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
    ${msg("loginTitle",realm.name)}
    <#elseif section = "header">
    ${msg("loginTitleHtml",realm.name)}
    <#elseif section = "form">
    <form id="kc-secondaf-selection-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
        <div class="${properties.kcFormGroupClass!}">
            <div class="${properties.kcLabelWrapperClass!}">
                <label for="secondaf" class="${properties.kcLabelClass!}">Select a second authentication factor</label>
            </div>

            <div class="${properties.kcInputWrapperClass!}">

                <select id="secondaf" name="secondaf">
                    <#list secondafoptions as secondafoption>
                        <option value="${secondafoption}">${secondafoption}</option>
                    </#list>
                </select>

            </div>
        </div>

        <div class="${properties.kcFormGroupClass!}">
            <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                <div class="${properties.kcFormOptionsWrapperClass!}">
                </div>
            </div>

            <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                <div class="${properties.kcFormButtonsWrapperClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}" name="cancel" id="kc-cancel" type="submit" value="${msg("doCancel")}"/>
                </div>
            </div>
        </div>
    </form>
    </#if>
</@layout.registrationLayout>