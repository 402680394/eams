package com.ztdx.eams.controller;

import com.ztdx.eams.domain.system.application.ResourceService;
import com.ztdx.eams.domain.system.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/resource")
public class ResourceController {

    private ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Resource resource) {
        resourceService.save(resource);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        resourceService.delete(id);
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody Resource resource) {
        resourceService.update(resource);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Resource get(@PathVariable("id") long id) {
        return resourceService.getResource(id);
    }

    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PATCH)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        resourceService.priority(upId, downId);
    }
}
